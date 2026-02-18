package com.afonsobenedito.werewolf.web.api

import com.afonsobenedito.werewolf.web.api.model.*
import com.afonsobenedito.werewolf.web.service.GameService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = ["*"])
class GameController(
    private val gameService: GameService
) {

    @PostMapping
    fun createGame(@RequestBody request: CreateGameRequest): ResponseEntity<Map<String, String>> {
        val gameId = gameService.createGame(request.mode, request.playerName, request.players)
        return ResponseEntity.ok(mapOf("gameId" to gameId))
    }

    @PostMapping("/{id}/join")
    fun joinGame(@PathVariable id: String, @RequestBody request: JoinGameRequest): ResponseEntity<Map<String, String>> {
        val player = gameService.joinGame(id, request.playerName)
        return if (player != null) {
            ResponseEntity.ok(mapOf("message" to "Joined successfully", "playerId" to player.name))
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
        @RequestParam(required = false) playerId: String?
    ): ResponseEntity<GameResponse> {
        val gameState = gameService.getGameState(id, playerId)
        return if (gameState != null) {
            ResponseEntity.ok(gameState)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/action")
    fun performAction(@PathVariable id: String, @RequestBody request: ActionRequest): ResponseEntity<Map<String, String>> {
        return try {
            val result = gameService.performAction(id, request)
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
