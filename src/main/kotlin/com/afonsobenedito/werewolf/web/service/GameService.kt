package com.afonsobenedito.werewolf.web.service

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.core.GamePhase
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.web.api.model.ActionRequest
import com.afonsobenedito.werewolf.web.api.model.GameResponse
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.web.repository.GameRepository
import com.afonsobenedito.werewolf.web.mapper.GameMapper
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val gameNotificationService: GameNotificationService,
    private val gameMapper: GameMapper
) {

    fun createGame(mode: GameMode, hostName: String?, players: List<String>? = null): String {
        val gameId = UUID.randomUUID().toString().substring(0, 8)
        val game = Game(gameId, "Werewolf-$gameId", mode)
        
        if (mode == GameMode.ONLINE && hostName != null) {
             val host = Player(hostName)
             game.players.add(host)
        }
        
        players?.forEach { name ->
            if (game.players.none { it.name == name }) {
                game.players.add(Player(name))
            }
        }
        
        saveGame(game)
        return gameId
    }

    fun getGame(gameId: String): Game? {
        return gameRepository.load(gameId)
    }
    
    fun getGameState(gameId: String, playerId: String?): GameResponse? {
        val game = gameRepository.load(gameId) ?: return null
        return gameMapper.toGameResponse(game, playerId)
    }

    private fun saveGame(game: Game) {
        gameRepository.save(game)
        gameNotificationService.notifyGameUpdate(game.id)
    }
    
    fun joinGame(gameId: String, playerName: String): Player? {
        val game = gameRepository.load(gameId) ?: return null
        if (game.status != GameStatus.NOT_STARTED) return null
        if (game.players.any { it.name == playerName }) return null
        
        val player = Player(playerName)
        game.players.add(player)
        
        saveGame(game)
        return player
    }

    fun startGame(gameId: String) {
        val game = gameRepository.load(gameId) ?: return
        if (game.players.size < 4) { 
             throw IllegalArgumentException("Need at least 4 players")
        }
        game.startGame()
        saveGame(game)
    }

    fun performAction(gameId: String, request: ActionRequest): String? {
        val game = gameRepository.load(gameId) ?: throw IllegalArgumentException("Game not found")
        
        if (game.mode == GameMode.OFFLINE) {
            if (request.playerId == "Master" || request.actionType == "VOTE") {
                val result = game.processOfflineAction(request.actionType, request.playerId, request.targetId)
                saveGame(game)
                if (game.phase == GamePhase.FINISHED) {
                   gameRepository.setShortTtl(game.id)
                }
                return result
            }
        }

        val player = game.players.find { it.name == request.playerId } ?: throw IllegalArgumentException("Player not found")
        
        // if (!player.isAlive) throw IllegalArgumentException("Player is dead") // Moved to specific phases
        
        val result = when (game.phase) {
            GamePhase.NIGHT -> {
                if (!player.isAlive) throw IllegalArgumentException("Player is dead")
                game.handleNightAction(player, request.actionType, request.targetId)
            }
            GamePhase.DAY_DISCUSSION -> {
                if (!player.isAlive) throw IllegalArgumentException("Player is dead")
                println("DAY_DISCUSSION Action: ${request.actionType} from ${player.name}")
                if (request.actionType == "READY_TO_VOTE") {
                    println("Adding ${player.name} to ready players")
                    game.readyPlayers.add(player.name)
                    val aliveCount = game.players.count { it.isAlive }
                    println("Ready count: ${game.readyPlayers.size} / $aliveCount")
                    if (game.readyPlayers.size >= aliveCount) {
                        println("Advancing phase to VOTING")
                        game.advancePhase()
                    }
                    null
                } else {
                    println("Invalid action during discussion: ${request.actionType}")
                    throw IllegalArgumentException("Cannot act during discussion")
                }
            }
            GamePhase.DAY_VOTING -> { 
                if (!player.isAlive) throw IllegalArgumentException("Player is dead")
                game.handleVotingAction(player, request.actionType, request.targetId); null 
            }
            GamePhase.DAY_RESULTS -> {
                // Allow dead players (HOST) to continue
                if (request.actionType == "CONTINUE") {
                    game.advancePhase()
                    null
                } else {
                     throw IllegalArgumentException("Cannot act during results")
                }
            }
            GamePhase.FINISHED -> throw IllegalArgumentException("Game is already finished")
        }
        
        game.checkPhaseTransition()
        saveGame(game)
        
        if (game.phase == GamePhase.FINISHED) {
             gameRepository.setShortTtl(game.id)
        }
        
        return result
    }
}

