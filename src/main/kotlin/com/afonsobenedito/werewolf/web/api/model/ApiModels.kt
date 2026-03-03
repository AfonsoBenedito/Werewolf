package com.afonsobenedito.werewolf.web.api.model

import com.afonsobenedito.werewolf.core.GameMode

data class CreateGameRequest(
    val mode: GameMode,
    val playerName: String? = null,
    val players: List<String>? = null
)

data class JoinGameRequest(
    val playerName: String
)

data class GameResponse(
    val id: String,
    val status: String,
    val players: List<PlayerResponse>,
    val phase: String,
    val dayCount: Int,
    val winner: String? = null,
    val lastDeadPlayerName: String? = null,
    val votes: Map<String, String> = emptyMap(),
    val readyPlayerCount: Int = 0,
    val totalAliveCount: Int = 0,
    val nightStatus: String? = null,
    val phaseKey: String = "",
    val currentTurn: String? = null,
    val isMyTurn: Boolean = false,
    val availableActions: List<String> = emptyList(),
    val canSkip: Boolean = false,
    val phaseDisplayName: String = "",
    val turnInstruction: String? = null,
    val canStart: Boolean = false,
    val isHost: Boolean = false,
    val minPlayers: Int = 4
)

data class PlayerResponse(
    val name: String,
    val isAlive: Boolean,
    val role: String? = null,
    val isTargetable: Boolean = false,
    val hasVoted: Boolean = false,
    val isOnWinningTeam: Boolean? = null
)

data class ActionRequest(
    val playerId: String = "",
    val actionType: String,
    val targetId: String? = null
)
