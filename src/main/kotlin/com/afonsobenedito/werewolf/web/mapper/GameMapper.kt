package com.afonsobenedito.werewolf.web.mapper

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.core.GamePhase
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.core.Winner
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Medic
import com.afonsobenedito.werewolf.core.model.roles.Seer
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.web.api.model.GameResponse
import com.afonsobenedito.werewolf.web.api.model.PlayerResponse
import org.springframework.stereotype.Component

@Component
class GameMapper {

    companion object {
        const val MIN_PLAYERS = 4
    }

    fun toGameResponse(game: Game, playerId: String?): GameResponse {
        val displayPhase = if (game.phase == GamePhase.NIGHT) {
            "NIGHT - ${game.currentTurn}"
        } else {
            game.phase.name
        }

        val requester = if (playerId != null) game.players.find { it.name == playerId } else null
        val isHost = playerId != null && game.players.firstOrNull()?.name == playerId
        val currentTurn = if (game.phase == GamePhase.NIGHT) game.currentTurn else null

        return GameResponse(
            id = game.id,
            status = game.status.name,
            players = game.players.map { p ->
                PlayerResponse(
                    name = p.name,
                    isAlive = p.isAlive,
                    role = if (shouldRevealRole(game, p, requester)) p.role?.name ?: "Unknown" else "Unknown",
                    isTargetable = computeIsTargetable(game, p, requester),
                    hasVoted = game.phase == GamePhase.DAY_VOTING && game.votes.containsKey(p.name),
                    isOnWinningTeam = computeIsOnWinningTeam(game, p)
                )
            },
            phase = displayPhase,
            dayCount = game.dayCount,
            winner = game.winner?.name,
            lastDeadPlayerName = game.lastDeadPlayerName,
            votes = maskVotes(game, playerId),
            readyPlayerCount = game.readyPlayers.size,
            totalAliveCount = game.players.count { it.isAlive },
            nightStatus = computeNightStatus(game, playerId),
            phaseKey = game.phase.name,
            currentTurn = currentTurn,
            isMyTurn = computeIsMyTurn(game, requester),
            availableActions = computeAvailableActions(game, requester),
            canSkip = computeCanSkip(game, requester),
            phaseDisplayName = computePhaseDisplayName(game),
            turnInstruction = computeTurnInstruction(game),
            canStart = isHost && game.status == GameStatus.NOT_STARTED && game.players.size >= MIN_PLAYERS,
            isHost = isHost,
            minPlayers = MIN_PLAYERS
        )
    }

    private fun computeNightStatus(game: Game, playerId: String?): String? {
        if (playerId == null || game.currentTurn != Game.TURN_WOLF) return null
        val player = game.players.find { it.name == playerId }
        if (player?.role !is Wolf) return null

        val status = game.getWolfStatus() ?: return null
        if (status == "Waiting for other werewolf..." && !game.wolfVotes.containsKey(playerId)) {
            return null
        }
        return status
    }

    private fun computeIsMyTurn(game: Game, requester: Player?): Boolean {
        if (game.status != GameStatus.IN_PROGRESS) return false
        if (requester != null && !requester.isAlive) return false

        return when (game.phase) {
            GamePhase.NIGHT -> {
                if (requester == null) return false
                when (game.currentTurn) {
                    Game.TURN_WOLF -> requester.role is Wolf
                    Game.TURN_SEER -> requester.role is Seer
                    Game.TURN_MEDIC -> requester.role is Medic
                    else -> false
                }
            }
            GamePhase.DAY_DISCUSSION, GamePhase.DAY_VOTING -> true
            else -> false
        }
    }

    private fun computeAvailableActions(game: Game, requester: Player?): List<String> {
        if (game.status != GameStatus.IN_PROGRESS) return emptyList()
        if (requester != null && !requester.isAlive) return emptyList()

        return when (game.phase) {
            GamePhase.NIGHT -> {
                when (game.currentTurn) {
                    Game.TURN_WOLF -> listOf(Game.ACTION_KILL)
                    Game.TURN_SEER -> listOf(Game.ACTION_PEEK)
                    Game.TURN_MEDIC -> listOf(Game.ACTION_HEAL)
                    else -> emptyList()
                }
            }
            GamePhase.DAY_DISCUSSION -> listOf("READY_TO_VOTE")
            GamePhase.DAY_VOTING -> listOf(Game.ACTION_VOTE, Game.VOTE_ABSTAIN)
            else -> emptyList()
        }
    }

    private fun computeCanSkip(game: Game, requester: Player?): Boolean {
        if (game.phase != GamePhase.NIGHT) return false
        if (requester == null) return game.currentTurn == Game.TURN_SEER || game.currentTurn == Game.TURN_MEDIC
        return when {
            requester.role is Seer && game.currentTurn == Game.TURN_SEER -> true
            requester.role is Medic && game.currentTurn == Game.TURN_MEDIC -> true
            else -> false
        }
    }

    private fun computeIsTargetable(game: Game, player: Player, requester: Player?): Boolean {
        if (!player.isAlive) return false
        if (game.status != GameStatus.IN_PROGRESS) return false

        return when (game.phase) {
            GamePhase.NIGHT -> when (game.currentTurn) {
                Game.TURN_WOLF -> player.role !is Wolf
                Game.TURN_SEER -> if (requester != null && game.mode != GameMode.OFFLINE) player.name != requester.name else player.role !is Seer
                Game.TURN_MEDIC -> true
                else -> false
            }
            GamePhase.DAY_VOTING -> requester == null || player.name != requester.name
            else -> false
        }
    }

    private fun computeIsOnWinningTeam(game: Game, player: Player): Boolean? {
        if (game.status != GameStatus.FINISHED || game.winner == null) return null
        return when (game.winner) {
            Winner.WEREWOLVES -> player.role is Wolf
            Winner.VILLAGERS -> player.role !is Wolf
            null -> null
        }
    }

    private fun computePhaseDisplayName(game: Game): String {
        return when (game.phase) {
            GamePhase.NIGHT -> when (game.currentTurn) {
                Game.TURN_WOLF -> "Werewolves' Turn"
                Game.TURN_SEER -> "Seer's Turn"
                Game.TURN_MEDIC -> "Medic's Turn"
                else -> "Night Phase"
            }
            GamePhase.DAY_DISCUSSION -> "Village Discussion"
            GamePhase.DAY_VOTING -> "Voting Phase"
            GamePhase.DAY_RESULTS -> "Voting Results"
            GamePhase.FINISHED -> "Game Over"
        }
    }

    private fun computeTurnInstruction(game: Game): String? {
        if (game.status != GameStatus.IN_PROGRESS) return null

        return when (game.phase) {
            GamePhase.NIGHT -> when (game.currentTurn) {
                Game.TURN_WOLF -> "Choose a victim to kill."
                Game.TURN_SEER -> "Choose a player to inspect."
                Game.TURN_MEDIC -> "Choose a player to save."
                else -> null
            }
            GamePhase.DAY_DISCUSSION -> "Discuss who might be a wolf."
            GamePhase.DAY_VOTING -> "Vote to eliminate a suspect."
            else -> null
        }
    }

    private fun maskVotes(game: Game, playerId: String?): Map<String, String> {
        return if (game.phase == GamePhase.DAY_VOTING) {
            game.votes.mapValues { (voter, target) ->
                if (voter == playerId) target else "SECRET"
            }
        } else {
            game.votes
        }
    }

    private fun shouldRevealRole(game: Game, player: Player, requester: Player?): Boolean {
        if (game.status == GameStatus.FINISHED) return true
        if (!player.isAlive) return true
        if (player.name == requester?.name) return true
        if (game.mode == GameMode.OFFLINE) return true
        if (requester?.role is Wolf && player.role is Wolf) return true
        return false
    }
}
