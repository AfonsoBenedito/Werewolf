package com.afonsobenedito.werewolf.web.mapper

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.core.GamePhase
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.web.api.model.GameResponse
import com.afonsobenedito.werewolf.web.api.model.PlayerResponse
import org.springframework.stereotype.Component

@Component
class GameMapper {

    fun toGameResponse(game: Game, playerId: String?): GameResponse {
        // For Offline mode, the "phase" should include whose turn it is
        val displayPhase = if (game.phase == GamePhase.NIGHT) {
            "NIGHT - ${game.currentTurn}"
        } else {
            game.phase.name
        }

        return GameResponse(
            id = game.id,
            status = game.status.name,
            players = game.players.map { p ->
                PlayerResponse(
                    id = p.name,
                    name = p.name,
                    isAlive = p.isAlive,
                    role = if (shouldRevealRole(game, p, playerId)) p.role?.name ?: "Unknown" else "Unknown"
                )
            },
            mode = game.mode,
            phase = displayPhase,
            dayCount = game.dayCount,
            winner = game.winner?.name,
            lastDeadPlayerName = game.lastDeadPlayerName,
            votes = maskVotes(game, playerId),
            readyPlayerCount = game.readyPlayers.size,
            totalAliveCount = game.players.count { it.isAlive }
        )
    }

    private fun maskVotes(game: Game, playerId: String?): Map<String, String> {
        return if (game.phase == GamePhase.DAY_VOTING) {
            // Mask votes: Show own vote target, else "SECRET" (meaning they voted)
            game.votes.mapValues { (voter, target) ->
                if (voter == playerId) target else "SECRET"
            }
        } else {
            game.votes // Show all results after voting is done (in Results phase)
        }
    }

    private fun shouldRevealRole(game: Game, player: Player, requesterId: String?): Boolean {
        if (game.status == GameStatus.FINISHED) return true
        if (!player.isAlive) return true
        if (player.name == requesterId) return true
        if (game.mode == GameMode.OFFLINE) return true

        if (requesterId != null) {
            val requester = game.players.find { it.name == requesterId }
            if (requester?.role is Wolf && player.role is Wolf) {
                return true
            }
        }
        return false
    }
}
