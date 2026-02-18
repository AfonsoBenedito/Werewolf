package com.afonsobenedito.werewolf.web.service

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.core.GamePhase
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*
import com.afonsobenedito.werewolf.web.api.model.ActionRequest
import com.afonsobenedito.werewolf.web.mapper.GameMapper
import com.afonsobenedito.werewolf.web.repository.GameRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Spy
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class GameServiceTest {

    @Mock
    lateinit var gameRepository: GameRepository

    @Mock
    lateinit var gameNotificationService: GameNotificationService

    @Spy
    val gameMapper: GameMapper = GameMapper()

    @InjectMocks
    lateinit var gameService: GameService

    // --- Helpers ---

    private fun createGameWithRoles(
        id: String = "test-game",
        mode: GameMode = GameMode.ONLINE,
        playerRoles: List<Pair<String, Role>> = listOf(
            "Wolf1" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "Villager1" to Villager()
        )
    ): Game {
        val game = Game(id, "Test", mode)
        playerRoles.forEach { (name, role) ->
            val player = Player(name)
            player.assignRole(role)
            game.addPlayer(player)
        }
        return game
    }

    private fun startedGame(
        id: String = "test-game",
        mode: GameMode = GameMode.ONLINE
    ): Game {
        val game = Game(id, "Test", mode)
        (1..4).forEach { game.addPlayer(Player("P$it")) }
        game.startGame()
        return game
    }

    // ===================
    // createGame
    // ===================

    @Test
    fun `createGame saves game and notifies`() {
        val gameId = gameService.createGame(GameMode.ONLINE, "Host")

        val gameCaptor = argumentCaptor<Game>()
        verify(gameRepository).save(gameCaptor.capture())
        assertEquals(gameId, gameCaptor.firstValue.id)
        verify(gameNotificationService).notifyGameUpdate(gameId)
    }

    @Test
    fun `createGame online adds host as player`() {
        gameService.createGame(GameMode.ONLINE, "Host")

        val gameCaptor = argumentCaptor<Game>()
        verify(gameRepository).save(gameCaptor.capture())
        assertTrue(gameCaptor.firstValue.players.any { it.name == "Host" })
    }

    @Test
    fun `createGame offline with player list adds all players`() {
        gameService.createGame(GameMode.OFFLINE, null, listOf("A", "B", "C", "D"))

        val gameCaptor = argumentCaptor<Game>()
        verify(gameRepository).save(gameCaptor.capture())
        assertEquals(4, gameCaptor.firstValue.players.size)
        assertEquals(listOf("A", "B", "C", "D"), gameCaptor.firstValue.players.map { it.name })
    }

    @Test
    fun `createGame offline deduplicates player names`() {
        gameService.createGame(GameMode.OFFLINE, null, listOf("A", "B", "A"))

        val gameCaptor = argumentCaptor<Game>()
        verify(gameRepository).save(gameCaptor.capture())
        assertEquals(2, gameCaptor.firstValue.players.size)
    }

    // ===================
    // getGameState
    // ===================

    @Test
    fun `getGameState loads game and maps response`() {
        val game = Game("test-game", "Test", GameMode.ONLINE)
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val response = gameService.getGameState("test-game", "Player1")

        assertNotNull(response)
        assertEquals("test-game", response?.id)
        verify(gameMapper).toGameResponse(game, "Player1")
    }

    @Test
    fun `getGameState returns null when game not found`() {
        whenever(gameRepository.load("missing")).thenReturn(null)

        val response = gameService.getGameState("missing", "Player1")

        assertNull(response)
    }

    // ===================
    // joinGame
    // ===================

    @Test
    fun `joinGame adds player saves and notifies`() {
        val game = Game("test-game", "Test", GameMode.ONLINE)
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val player = gameService.joinGame("test-game", "NewPlayer")

        assertNotNull(player)
        assertEquals("NewPlayer", player?.name)
        assertTrue(game.players.any { it.name == "NewPlayer" })
        verify(gameRepository).save(game)
        verify(gameNotificationService).notifyGameUpdate("test-game")
    }

    @Test
    fun `joinGame returns null when game not found`() {
        whenever(gameRepository.load("missing")).thenReturn(null)

        val result = gameService.joinGame("missing", "Player")

        assertNull(result)
    }

    @Test
    fun `joinGame returns null when game already started`() {
        val game = startedGame()
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val result = gameService.joinGame("test-game", "LatePlayer")

        assertNull(result)
        verify(gameRepository, never()).save(game)
    }

    @Test
    fun `joinGame returns null when name already taken`() {
        val game = Game("test-game", "Test", GameMode.ONLINE)
        game.addPlayer(Player("ExistingPlayer"))
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val result = gameService.joinGame("test-game", "ExistingPlayer")

        assertNull(result)
        assertEquals(1, game.players.size)
    }

    // ===================
    // startGame
    // ===================

    @Test
    fun `startGame starts game saves and notifies`() {
        val game = Game("test-game", "Test", GameMode.ONLINE)
        (1..4).forEach { game.addPlayer(Player("P$it")) }
        whenever(gameRepository.load("test-game")).thenReturn(game)

        gameService.startGame("test-game")

        assertEquals(GameStatus.IN_PROGRESS, game.status)
        verify(gameRepository).save(game)
        verify(gameNotificationService).notifyGameUpdate("test-game")
    }

    @Test
    fun `startGame throws when game not found`() {
        whenever(gameRepository.load("missing")).thenReturn(null)

        val ex = assertThrows<IllegalArgumentException> {
            gameService.startGame("missing")
        }
        assertEquals("Game not found", ex.message)
    }

    @Test
    fun `startGame throws when not enough players`() {
        val game = Game("test-game", "Test", GameMode.ONLINE)
        game.addPlayer(Player("P1"))
        game.addPlayer(Player("P2"))
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val ex = assertThrows<IllegalArgumentException> {
            gameService.startGame("test-game")
        }
        assertEquals("Need at least 4 players", ex.message)
    }

    // ===================
    // performAction — game not found
    // ===================

    @Test
    fun `performAction throws when game not found`() {
        whenever(gameRepository.load("missing")).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            gameService.performAction("missing", ActionRequest("P1", "VOTE", "P2"))
        }
    }

    // ===================
    // performAction — offline mode
    // ===================

    @Test
    fun `performAction offline delegates to processOfflineAction`() {
        val game = createGameWithRoles(mode = GameMode.OFFLINE)
        game.phase = GamePhase.NIGHT
        game.dayCount = 1
        game.startNightPhase()
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val result = gameService.performAction(
            "test-game", ActionRequest("Master", "KILL", "Villager1")
        )

        assertNull(result)
        verify(gameRepository).save(game)
    }

    @Test
    fun `performAction offline vote delegates to processOfflineAction`() {
        val game = createGameWithRoles(mode = GameMode.OFFLINE)
        game.phase = GamePhase.DAY_VOTING
        whenever(gameRepository.load("test-game")).thenReturn(game)

        gameService.performAction("test-game", ActionRequest("Wolf1", "VOTE", "Seer1"))

        assertEquals("Seer1", game.votes["Wolf1"])
        verify(gameRepository).save(game)
    }

    @Test
    fun `performAction offline sets short TTL when game finishes`() {
        val game = createGameWithRoles(mode = GameMode.OFFLINE)
        // Kill everyone except wolf and one villager, then eliminate villager
        game.players.find { it.name == "Seer1" }!!.die()
        game.players.find { it.name == "Medic1" }!!.die()
        game.phase = GamePhase.DAY_VOTING
        whenever(gameRepository.load("test-game")).thenReturn(game)

        gameService.performAction("test-game", ActionRequest("Master", "ELIMINATE", "Villager1"))

        assertEquals(GamePhase.FINISHED, game.phase)
        verify(gameRepository).setShortTtl("test-game")
    }

    // ===================
    // performAction — online night
    // ===================

    @Test
    fun `performAction online night delegates to handleNightAction`() {
        val game = startedGame()
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val wolf = game.players.first { it.role is Wolf }
        val target = game.players.first { it.role !is Wolf }

        gameService.performAction(
            "test-game", ActionRequest(wolf.name, "KILL", target.name)
        )

        verify(gameRepository).save(game)
    }

    // ===================
    // performAction — online day discussion
    // ===================

    @Test
    fun `performAction day discussion READY_TO_VOTE marks player ready`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_DISCUSSION
        whenever(gameRepository.load("test-game")).thenReturn(game)

        gameService.performAction("test-game", ActionRequest("P1", "READY_TO_VOTE"))

        assertTrue(game.readyPlayers.contains("P1"))
        verify(gameRepository).save(game)
    }

    @Test
    fun `performAction day discussion all ready advances to voting`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_DISCUSSION
        whenever(gameRepository.load("test-game")).thenReturn(game)

        val alivePlayers = game.players.filter { it.isAlive }
        alivePlayers.forEach { p ->
            gameService.performAction("test-game", ActionRequest(p.name, "READY_TO_VOTE"))
        }

        assertEquals(GamePhase.DAY_VOTING, game.phase)
    }

    @Test
    fun `performAction day discussion invalid action throws`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_DISCUSSION
        whenever(gameRepository.load("test-game")).thenReturn(game)

        assertThrows<IllegalArgumentException> {
            gameService.performAction("test-game", ActionRequest("P1", "VOTE", "P2"))
        }
    }

    // ===================
    // performAction — online day voting
    // ===================

    @Test
    fun `performAction day voting delegates to handleVotingAction`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_VOTING
        whenever(gameRepository.load("test-game")).thenReturn(game)

        gameService.performAction("test-game", ActionRequest("P1", "VOTE", "P2"))

        assertEquals("P2", game.votes["P1"])
    }

    // ===================
    // performAction — online day results
    // ===================

    @Test
    fun `performAction day results CONTINUE marks player ready`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_RESULTS
        whenever(gameRepository.load("test-game")).thenReturn(game)

        gameService.performAction("test-game", ActionRequest("P1", "CONTINUE"))

        assertTrue(game.readyPlayers.contains("P1"))
    }

    @Test
    fun `performAction day results all continue advances to night`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_RESULTS
        whenever(gameRepository.load("test-game")).thenReturn(game)

        game.players.filter { it.isAlive }.forEach { p ->
            gameService.performAction("test-game", ActionRequest(p.name, "CONTINUE"))
        }

        assertEquals(GamePhase.NIGHT, game.phase)
    }

    @Test
    fun `performAction day results invalid action throws`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_RESULTS
        whenever(gameRepository.load("test-game")).thenReturn(game)

        assertThrows<IllegalArgumentException> {
            gameService.performAction("test-game", ActionRequest("P1", "VOTE", "P2"))
        }
    }

    // ===================
    // performAction — finished game
    // ===================

    @Test
    fun `performAction on finished game throws`() {
        val game = startedGame()
        game.phase = GamePhase.FINISHED
        whenever(gameRepository.load("test-game")).thenReturn(game)

        assertThrows<IllegalArgumentException> {
            gameService.performAction("test-game", ActionRequest("P1", "VOTE", "P2"))
        }
    }

    // ===================
    // performAction — dead player
    // ===================

    @Test
    fun `performAction as dead player throws`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_VOTING
        game.players.find { it.name == "P1" }!!.die()
        whenever(gameRepository.load("test-game")).thenReturn(game)

        assertThrows<IllegalArgumentException> {
            gameService.performAction("test-game", ActionRequest("P1", "VOTE", "P2"))
        }
    }

    // ===================
    // performAction — player not found
    // ===================

    @Test
    fun `performAction with unknown player throws`() {
        val game = startedGame()
        game.phase = GamePhase.DAY_VOTING
        whenever(gameRepository.load("test-game")).thenReturn(game)

        assertThrows<IllegalArgumentException> {
            gameService.performAction("test-game", ActionRequest("Unknown", "VOTE", "P1"))
        }
    }
}
