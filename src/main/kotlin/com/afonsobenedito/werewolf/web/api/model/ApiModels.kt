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
    val mode: GameMode,
    val phase: String,
    val dayCount: Int,
    val winner: String? = null,
    val lastDeadPlayerName: String? = null,
    val votes: Map<String, String> = emptyMap(),
    val readyPlayerCount: Int = 0,
    val totalAliveCount: Int = 0
)

data class PlayerResponse(
    val id: String,
    val name: String,
    val isAlive: Boolean,
    val role: String? = null // Hidden for others in Online mode
)

data class ActionRequest(
    val playerId: String,
    val actionType: String, // VOTE, KILL, HEAL, PEEK
    val targetId: String? = null
)
