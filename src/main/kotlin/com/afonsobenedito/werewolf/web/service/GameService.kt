package com.afonsobenedito.werewolf.web.service

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.web.api.model.ActionRequest
import com.afonsobenedito.werewolf.web.api.model.GameMode
import com.afonsobenedito.werewolf.web.api.model.GameResponse
import com.afonsobenedito.werewolf.web.api.model.PlayerResponse
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class GameService(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val GAME_KEY_PREFIX = "game:"
        private const val GAME_TTL_MINUTES = 120L // 2 hours
    }

    fun createGame(mode: GameMode, hostName: String?): String {
        val gameId = UUID.randomUUID().toString().substring(0, 8)
        val game = Game("Werewolf-$gameId")
        val instance = GameInstance(id = gameId, game = game, mode = mode)
        
        if (mode == GameMode.ONLINE && hostName != null) {
             val host = Player(hostName)
             game.players.add(host)
        }
        
        saveGame(instance)
        return gameId
    }

    fun getGame(gameId: String): GameInstance? {
        return loadGame(gameId)
    }
    
    fun getGameState(gameId: String, playerId: String?): GameResponse? {
        val instance = loadGame(gameId) ?: return null
        val game = instance.game
        
        // For Offline mode, the "phase" should include whose turn it is
        val displayPhase = if (instance.phase == GamePhase.NIGHT) {
            "NIGHT - ${instance.currentTurn}"
        } else {
            instance.phase.name
        }
        
        return GameResponse(
            id = instance.id,
            status = game.status.name,
            players = game.players.map { p -> 
                PlayerResponse(
                    id = p.name, 
                    name = p.name,
                    isAlive = p.isAlive,
                    role = if (shouldRevealRole(instance, p, playerId)) p.role?.name ?: "Unknown" else "Unknown"
                )
            },
            mode = instance.mode,
            phase = displayPhase,
            dayCount = instance.dayCount,
            winner = game.winner?.name,
            lastDeadPlayerName = instance.lastDeadPlayerName,
            votes = instance.votes
        )
    }

    private fun loadGame(gameId: String): GameInstance? {
        val json = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId) ?: return null
        return try {
            objectMapper.readValue(json, GameInstance::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveGame(instance: GameInstance) {
        val json = objectMapper.writeValueAsString(instance)
        redisTemplate.opsForValue().set(
            GAME_KEY_PREFIX + instance.id,
            json,
            GAME_TTL_MINUTES,
            TimeUnit.MINUTES
        )
    }
    
    private fun deleteGame(gameId: String) {
        redisTemplate.delete(GAME_KEY_PREFIX + gameId)
    }

    private fun shouldRevealRole(instance: GameInstance, player: Player, requesterId: String?): Boolean {
        // Always reveal if game over or player dead
        if (instance.game.status == GameStatus.FINISHED || !player.isAlive) return true
        
        // Reveal to self
        if (player.name == requesterId) return true
        
        // Reveal to Master in Offline Mode
        // We send the data to the frontend, which handles the visual hiding/revealing.
        if (instance.mode == GameMode.OFFLINE) return true
        
        // Wolves see other Wolves
        if (requesterId != null) {
            val requester = instance.game.players.find { it.name == requesterId }
            if (requester?.role is Wolf && player.role is Wolf) return true
        }
        
        return false
    }

    fun joinGame(gameId: String, playerName: String): Player? {
        val instance = loadGame(gameId) ?: return null
        if (instance.game.status != GameStatus.NOT_STARTED) return null
        if (instance.game.players.any { it.name == playerName }) return null // Unique names
        
        val player = Player(playerName)
        instance.game.players.add(player)
        
        saveGame(instance)
        return player
    }

    fun startGame(gameId: String) {
        val instance = loadGame(gameId) ?: return
        if (instance.game.players.size < 4) { 
             throw IllegalArgumentException("Need at least 4 players")
        }
        instance.game.startGame()
        instance.phase = GamePhase.NIGHT
        instance.currentTurn = "Wolf" // Start with Wolf turn
        instance.dayCount = 1
        
        saveGame(instance)
    }
    
    fun performAction(gameId: String, request: ActionRequest) {
        val instance = loadGame(gameId) ?: throw IllegalArgumentException("Game not found")
        
        // Bypass player check for Offline Master actions OR Voting
        if (instance.mode == GameMode.OFFLINE) {
            if (request.playerId == "Master" || request.actionType == "VOTE") {
                processOfflineAction(instance, request)
                saveGame(instance) // Save after action processing (which may finish game)
                // Check if game finished and delete if so? No, user might want to see results.
                // Or maybe delete after a short delay? For now, standard TTL applies.
                // But specifically requested "delete after finished".
                // Maybe delete explicitly if FINISHED?
                if (instance.phase == GamePhase.FINISHED) {
                   // Keep it for a bit so they can see "Winner: X", then rely on TTL or manually delete?
                   // User said: "each game is deleted after it's finished".
                   // Let's assume immediate deletion might be too aggressive if they want to see result screen.
                   // Let's set a very short TTL (e.g. 5 mins) instead of immediate delete?
                   // Or just rely on standard TTL. 
                   // Plan said: "Explicitly call delete... or set distinct short TTL".
                   // Let's update TTL to 10 minutes if finished.
                   setShortTtl(instance.id)
                }
                return
            }
        }

        val player = instance.game.players.find { it.name == request.playerId } ?: throw IllegalArgumentException("Player not found")
        
        if (!player.isAlive) throw IllegalArgumentException("Player is dead")
        
        when (instance.phase) {
            GamePhase.NIGHT -> handleNightAction(instance, player, request)
            GamePhase.DAY_DISCUSSION -> throw IllegalArgumentException("Cannot act during discussion")
            GamePhase.DAY_VOTING -> handleVotingAction(instance, player, request)
            GamePhase.DAY_RESULTS -> throw IllegalArgumentException("Cannot act during results")
            GamePhase.FINISHED -> throw IllegalArgumentException("Game is already finished")
        }
        
        checkPhaseTransition(instance)
        saveGame(instance)
        
        if (instance.phase == GamePhase.FINISHED) {
             setShortTtl(instance.id)
        }
    }
    
    private fun setShortTtl(gameId: String) {
         redisTemplate.expire(GAME_KEY_PREFIX + gameId, 10, TimeUnit.MINUTES)
    }

    private fun handleNightAction(instance: GameInstance, actor: Player, request: ActionRequest) {
        // Online logic placeholder
    }
    
    private fun processOfflineAction(instance: GameInstance, request: ActionRequest) {
         val target = instance.game.players.find { it.name == request.targetId }
         
         when (request.actionType) {
             "KILL" -> {
                 if (target != null && target.role is Wolf) {
                     throw IllegalArgumentException("Wolves cannot kill other Wolves!")
                 }
                 if (target != null) instance.pendingDeathId = target.name
                 advanceTurn(instance)
             }
             "HEAL" -> { 
                 if (target != null && instance.pendingDeathId == target.name) {
                     instance.pendingDeathId = null // Saved!
                 }
                 advanceTurn(instance)
             }
             "PEEK" -> { 
                 advanceTurn(instance)
             }
             "NEXT_TURN" -> advanceTurn(instance) 
             "NEXT_PHASE" -> advancePhase(instance)
             "ELIMINATE" -> {
                 target?.die()
                 checkPhaseTransition(instance)
                 if (instance.phase != GamePhase.FINISHED) {
                    instance.phase = GamePhase.NIGHT
                    instance.dayCount++
                    instance.currentTurn = "Wolf"
                    instance.lastDeadPlayerName = null 
                 }
             }
             "VOTE" -> {
                 val voterId = request.playerId
                 if (voterId == "Master") return 
                 if (target == null) throw IllegalArgumentException("Vote target needed")
                 
                 if (instance.votes.containsKey(voterId)) {
                     throw IllegalArgumentException("Player $voterId has already voted!")
                 }
                 
                 instance.votes[voterId] = target.name
                 
                 val alivePlayers = instance.game.players.filter { it.isAlive }
                 
                 if (instance.votes.size >= alivePlayers.size) {
                     tallyVotes(instance)
                 }
             }
         }
         checkPhaseTransition(instance)
    }
    
    private fun tallyVotes(instance: GameInstance) {
        val voteCounts = instance.votes.values.groupingBy { it }.eachCount()
        val maxVotes = voteCounts.maxByOrNull { it.value }?.value ?: 0
        val candidates = voteCounts.filter { it.value == maxVotes }.keys
        
        if (candidates.size == 1) {
            val victimName = candidates.first()
            val victim = instance.game.players.find { it.name == victimName }
            victim?.die()
            instance.lastDeadPlayerName = victimName 
        } else {
            instance.lastDeadPlayerName = null 
        }
        
        instance.votes.clear()
        
        instance.phase = GamePhase.DAY_RESULTS
        instance.currentTurn = "" 
        
        checkPhaseTransition(instance)
    }

    
    private fun advanceTurn(instance: GameInstance) {
        if (instance.phase != GamePhase.NIGHT) return
        
        when (instance.currentTurn) {
            "Wolf" -> instance.currentTurn = "Seer"
            "Seer" -> instance.currentTurn = "Medic"
            "Medic" -> advancePhase(instance) // End of night
            else -> instance.currentTurn = "Wolf"
        }
    }
    
    private fun handleVotingAction(instance: GameInstance, voter: Player, request: ActionRequest) {
        // ... (Online voting logic to be implemented)
    }

    private fun checkPhaseTransition(instance: GameInstance) {
        if (instance.game.checkWinCondition()) {
            instance.phase = GamePhase.FINISHED
        }
    }
    
    private fun advancePhase(instance: GameInstance) {
        when (instance.phase) {
            GamePhase.NIGHT -> {
                if (instance.pendingDeathId != null) {
                    val victim = instance.game.players.find { it.name == instance.pendingDeathId }
                    victim?.die()
                    instance.lastDeadPlayerName = instance.pendingDeathId
                    instance.pendingDeathId = null
                } else {
                    instance.lastDeadPlayerName = null
                }
                
                instance.phase = GamePhase.DAY_DISCUSSION
                instance.currentTurn = ""
            }
            GamePhase.DAY_DISCUSSION -> {
                instance.phase = GamePhase.DAY_VOTING
            }
            GamePhase.DAY_VOTING -> {
                tallyVotes(instance) 
            }
            GamePhase.DAY_RESULTS -> {
                instance.phase = GamePhase.NIGHT
                instance.dayCount++
                instance.currentTurn = "Wolf"
                instance.lastDeadPlayerName = null 
            }
            GamePhase.FINISHED -> {} 
        }
        checkPhaseTransition(instance)
    }
}

enum class GamePhase {
    NIGHT, DAY_DISCUSSION, DAY_VOTING, DAY_RESULTS, FINISHED
}

data class GameInstance(
    val id: String,
    val game: Game,
    val mode: GameMode,
    var phase: GamePhase = GamePhase.NIGHT,
    var currentTurn: String = "", 
    var dayCount: Int = 0,
    var pendingDeathId: String? = null,
    var lastDeadPlayerName: String? = null,
    val votes: MutableMap<String, String> = mutableMapOf()
)
