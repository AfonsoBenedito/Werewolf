package com.afonsobenedito.werewolf.web.api.model

import com.afonsobenedito.werewolf.core.GameMode


data class CreateGameRequest(
    val mode: GameMode,
    val playerName: String? = null, // For Online mode, the host joins immediately
    val players: List<String>? = null // For Offline mode, bulk add
)

data class JoinGameRequest(
    val playerName: String
)

data class GameResponse(
    val id: String,
    val status: String,
    val players: List<PlayerResponse>,
    val phase: String, // Legacy format "NIGHT - Wolf" for useGameTransitions compatibility
    val dayCount: Int,
    val winner: String? = null,
    val lastDeadPlayerName: String? = null,
    val votes: Map<String, String> = emptyMap(),
    val readyPlayerCount: Int = 0,
    val totalAliveCount: Int = 0,
    val nightStatus: String? = null, // For Wolf consensus feedback
    val phaseKey: String = "", // "NIGHT", "DAY_DISCUSSION", "DAY_VOTING", "DAY_RESULTS", "FINISHED"
    val currentTurn: String? = null, // "Wolf"/"Seer"/"Medic" or null
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
    val role: String? = null, // Hidden for others in Online mode
    val isTargetable: Boolean = false,
    val hasVoted: Boolean = false,
    val isOnWinningTeam: Boolean? = null // Only set when game is FINISHED
)

data class ActionRequest(
    val playerId: String,
    val actionType: String, // VOTE, KILL, HEAL, PEEK
    val targetId: String? = null
)
