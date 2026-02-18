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
            game.addPlayer(Player(hostName))
        }

        players?.forEach { name ->
            if (game.players.none { it.name == name }) {
                game.addPlayer(Player(name))
            }
        }

        saveGame(game)
        return gameId
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
        game.addPlayer(player)

        saveGame(game)
        return player
    }

    fun startGame(gameId: String) {
        val game = gameRepository.load(gameId)
            ?: throw IllegalArgumentException("Game not found")
        if (game.players.size < 4) {
            throw IllegalArgumentException("Need at least 4 players")
        }
        game.startGame()
        saveGame(game)
    }

    fun performAction(gameId: String, request: ActionRequest): String? {
        val game = gameRepository.load(gameId)
            ?: throw IllegalArgumentException("Game not found")

        val result = if (game.mode == GameMode.OFFLINE &&
            (request.playerId == Game.OFFLINE_MASTER || request.actionType == "VOTE")
        ) {
            game.processOfflineAction(request.actionType, request.playerId, request.targetId)
        } else {
            performOnlineAction(game, request)
        }

        saveGame(game)
        if (game.phase == GamePhase.FINISHED) {
            gameRepository.setShortTtl(game.id)
        }
        return result
    }

    private fun performOnlineAction(game: Game, request: ActionRequest): String? {
        if (game.phase == GamePhase.FINISHED) {
            throw IllegalArgumentException("Game is already finished")
        }

        val player = game.players.find { it.name == request.playerId }
            ?: throw IllegalArgumentException("Player not found")
        if (!player.isAlive) throw IllegalArgumentException("Player is dead")

        return when (game.phase) {
            GamePhase.NIGHT -> game.handleNightAction(player, request.actionType, request.targetId)
            GamePhase.DAY_DISCUSSION -> {
                requireAction(request.actionType, "READY_TO_VOTE") { "Cannot act during discussion" }
                markReadyAndAdvance(game, player.name)
                null
            }
            GamePhase.DAY_VOTING -> {
                game.handleVotingAction(player, request.actionType, request.targetId)
                null
            }
            GamePhase.DAY_RESULTS -> {
                requireAction(request.actionType, "CONTINUE") { "Cannot act during results" }
                markReadyAndAdvance(game, player.name)
                null
            }
            GamePhase.FINISHED -> throw IllegalStateException("Unreachable")
        }
    }

    private fun requireAction(actual: String, expected: String, message: () -> String) {
        if (actual != expected) throw IllegalArgumentException(message())
    }

    private fun markReadyAndAdvance(game: Game, playerName: String) {
        game.markPlayerReady(playerName)
        if (game.readyPlayers.size >= game.players.count { it.isAlive }) {
            game.advancePhase()
        }
    }
}
