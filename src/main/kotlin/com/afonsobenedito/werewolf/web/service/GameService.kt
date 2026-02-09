package com.afonsobenedito.werewolf.web.service

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.web.api.model.ActionRequest
import com.afonsobenedito.werewolf.web.api.model.GameMode
import com.afonsobenedito.werewolf.web.api.model.GameResponse
import com.afonsobenedito.werewolf.web.api.model.PlayerResponse
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class GameService {
    private val games = ConcurrentHashMap<String, GameInstance>()

    fun createGame(mode: GameMode, hostName: String?): String {
        val gameId = UUID.randomUUID().toString().substring(0, 8)
        val game = Game("Werewolf-$gameId")
        val instance = GameInstance(id = gameId, game = game, mode = mode)
        
        if (mode == GameMode.ONLINE && hostName != null) {
             val host = Player(hostName)
             game.players.add(host)
        }
        
        games[gameId] = instance
        return gameId
    }

    fun getGame(gameId: String): GameInstance? {
        return games[gameId]
    }
    
    fun getGameState(gameId: String, playerId: String?): GameResponse? {
        val instance = games[gameId] ?: return null
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

    private fun isWolf(game: Game, playerId: String?): Boolean {
        if (playerId == null) return false
        val player = game.players.find { it.name == playerId }
        return player?.role is Wolf
    }

    fun joinGame(gameId: String, playerName: String): Player? {
        val instance = games[gameId] ?: return null
        if (instance.game.status != GameStatus.NOT_STARTED) return null
        if (instance.game.players.any { it.name == playerName }) return null // Unique names
        
        val player = Player(playerName)
        instance.game.players.add(player)
        return player
    }

    fun startGame(gameId: String) {
        val instance = games[gameId] ?: return
        if (instance.game.players.size < 4) { 
             throw IllegalArgumentException("Need at least 4 players")
        }
        instance.game.startGame()
        instance.phase = GamePhase.NIGHT
        instance.currentTurn = "Wolf" // Start with Wolf turn
        instance.dayCount = 1
    }
    
    fun performAction(gameId: String, request: ActionRequest) {
        val instance = games[gameId] ?: throw IllegalArgumentException("Game not found")
        
        // Bypass player check for Offline Master actions OR Voting
        if (instance.mode == GameMode.OFFLINE) {
            if (request.playerId == "Master" || request.actionType == "VOTE") {
                processOfflineAction(instance, request)
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
                 // Just advance. Frontend will handle the "Peek" display logic via a separate call or alert?
                 // Or we can rely on text response if we change return type differently.
                 // For now, let's just advance. The Master effectively "simulates" the peek.
                 // If the User wants the APP to show the role, we should probably output it.
                 // But API returns generic success message.
                 // Let's assume Master physically checks or we might need a distinct "Reveal" endpoint.
                 advanceTurn(instance)
             }
             "NEXT_TURN" -> advanceTurn(instance) // Manual advance
             "NEXT_PHASE" -> advancePhase(instance)
             "ELIMINATE" -> {
                 // Deprecated in favor of individual voting, but keeping for manual override
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
                 if (voterId == "Master") return // Master shouldn't vote as Master
                 if (target == null) throw IllegalArgumentException("Vote target needed")
                 
                 // Prevent changing vote
                 if (instance.votes.containsKey(voterId)) {
                     throw IllegalArgumentException("Player $voterId has already voted!")
                 }
                 
                 // Record vote
                 instance.votes[voterId] = target.name
                 
                 // Check if everyone voted
                 val alivePlayers = instance.game.players.filter { it.isAlive }
                 // Debug print
                 println("Votes: ${instance.votes.size}, Alive: ${alivePlayers.size}")
                 
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
        
        println("Tallying votes... Candidates: $candidates")

        // Simple majority: if tie, no one dies (or random? user didn't specify).
        // Let's implement: Tie = No Death for now.
        if (candidates.size == 1) {
            val victimName = candidates.first()
            val victim = instance.game.players.find { it.name == victimName }
            victim?.die()
            instance.lastDeadPlayerName = victimName 
        } else {
            instance.lastDeadPlayerName = null // Tie or no votes -> No one dies
        }
        
        instance.votes.clear()
        
        // Transition to Results phase
        instance.phase = GamePhase.DAY_RESULTS
        instance.currentTurn = "" // No specific turn
        // lastDeadPlayerName is already set above
        
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
                // Apply Night Deaths
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
                tallyVotes(instance) // Manual trigger if needed, or just force tally
            }
            GamePhase.DAY_RESULTS -> {
                instance.phase = GamePhase.NIGHT
                instance.dayCount++
                instance.currentTurn = "Wolf"
                instance.lastDeadPlayerName = null // Reset for fresh night
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
    var currentTurn: String = "", // Wolf, Seer, Medic
    var dayCount: Int = 0,
    var pendingDeathId: String? = null,
    var lastDeadPlayerName: String? = null,
    val votes: MutableMap<String, String> = mutableMapOf()
)
