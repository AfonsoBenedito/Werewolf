package com.afonsobenedito.werewolf.web.service

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.web.mapper.GameMapper
import com.afonsobenedito.werewolf.web.repository.GameRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Spy

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

    @Test
    fun `createGame saves game and notifies`() {
        val gameId = gameService.createGame(GameMode.ONLINE, "Host")
        
        val gameCaptor = ArgumentCaptor.forClass(Game::class.java)
        verify(gameRepository).save(capture(gameCaptor))
        assertEquals(gameId, gameCaptor.value.id)
        
        verify(gameNotificationService).notifyGameUpdate(gameId)
    }

    private fun <T> capture(captor: ArgumentCaptor<T>): T {
        captor.capture()
        return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T

    @Test
    fun `getGameState loads game and maps response`() {
        val gameId = "test-game"
        val game = Game(gameId, "Test", GameMode.ONLINE)
        
        `when`(gameRepository.load(gameId)).thenReturn(game)
        
        val response = gameService.getGameState(gameId, "Player1")
        
        assertNotNull(response)
        assertEquals(gameId, response?.id)
        verify(gameRepository).load(gameId)
        verify(gameMapper).toGameResponse(game, "Player1")
    }

    @Test
    fun `joinGame loads, adds player, saves and notifies`() {
        val gameId = "test-game"
        val game = Game(gameId, "Test", GameMode.ONLINE)
        
        `when`(gameRepository.load(gameId)).thenReturn(game)
        
        val player = gameService.joinGame(gameId, "NewPlayer")
        
        assertNotNull(player)
        assertEquals("NewPlayer", player?.name)
        assertTrue(game.players.any { it.name == "NewPlayer" })
        
        verify(gameRepository).save(game)
        verify(gameNotificationService).notifyGameUpdate(gameId)
    }
}
