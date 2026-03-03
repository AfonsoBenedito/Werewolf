package com.afonsobenedito.werewolf.web.api

import com.afonsobenedito.werewolf.web.api.model.*
import com.afonsobenedito.werewolf.web.service.GameService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = ["*"])
class GameController(
    private val gameService: GameService
) {

    companion object {
        const val TOKEN_HEADER = "X-Player-Token"
    }

    @PostMapping
    fun createGame(@RequestBody request: CreateGameRequest): ResponseEntity<Map<String, String>> {
        val (gameId, token) = gameService.createGame(request.mode, request.playerName, request.players)
        return ResponseEntity.ok(mapOf("gameId" to gameId, "token" to token))
    }

    @PostMapping("/{id}/join")
    fun joinGame(@PathVariable id: String, @RequestBody request: JoinGameRequest): ResponseEntity<Map<String, String>> {
        val token = gameService.joinGame(id, request.playerName)
        return if (token != null) {
            ResponseEntity.ok(mapOf("token" to token))
        } else {
            ResponseEntity.badRequest().body(mapOf("message" to "Failed to join"))
        }
    }

    @PostMapping("/{id}/start")
    fun startGame(@PathVariable id: String): ResponseEntity<Map<String, String>> {
        return try {
            gameService.startGame(id)
            ResponseEntity.ok(mapOf("message" to "Game started"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "Unknown error")))
        }
    }

    @GetMapping("/{id}")
    fun getGameState(
        @PathVariable id: String,
        @RequestHeader(value = TOKEN_HEADER, required = false) token: String?
    ): ResponseEntity<GameResponse> {
        val playerName = if (token != null) gameService.resolveToken(token, id) else null
        val gameState = gameService.getGameState(id, playerName)
        return if (gameState != null) {
            ResponseEntity.ok(gameState)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/action")
    fun performAction(
        @PathVariable id: String,
        @RequestBody request: ActionRequest,
        @RequestHeader(value = TOKEN_HEADER, required = false) token: String?
    ): ResponseEntity<Map<String, String>> {
        val effectiveRequest = if (token != null) {
            val playerName = gameService.resolveToken(token, id)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("message" to "Invalid or expired session"))
            request.copy(playerId = playerName)
        } else {
            request
        }
        return try {
            val result = gameService.performAction(id, effectiveRequest)
            val response = buildMap {
                put("message", "Action accepted")
                if (result != null) put("peekResult", result)
            }
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "Unknown error")))
        }
    }
}
