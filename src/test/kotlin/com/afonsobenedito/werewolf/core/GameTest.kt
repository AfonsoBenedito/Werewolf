package com.afonsobenedito.werewolf.core

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GameTest {

    @Test
    fun `test initial game state`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        assertEquals("test-id", game.id)
        assertEquals("Test Game", game.name)
        assertEquals(GameMode.ONLINE, game.mode)
        assertEquals(GameStatus.NOT_STARTED, game.status)
        assertEquals(GamePhase.NIGHT, game.phase)
    }

    @Test
    fun `test start game assigns roles and starts night`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        val players = (1..6).map { Player("Player$it") }
        game.addPlayers(players)
        
        game.startGame()
        
        assertEquals(GameStatus.IN_PROGRESS, game.status)
        assertEquals(GamePhase.NIGHT, game.phase)
        assertEquals(1, game.dayCount)
        assertEquals("Wolf", game.currentTurn)
        
        // precise role counts for 6 players: 2 Wolves, 1 Seer, 1 Medic, 2 Villagers
        val wolves = game.players.count { it.role is Wolf }
        val seers = game.players.count { it.role is Seer }
        val medics = game.players.count { it.role is Medic }
        val villagers = game.players.count { it.role is Villager }
        
        assertEquals(2, wolves)
        assertEquals(1, seers)
        assertEquals(1, medics)
        assertEquals(2, villagers)
    }
    
    @Test
    fun `test night phase turn progression`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        val p1 = Player("P1"); p1.assignRole(Wolf())
        val p2 = Player("P2"); p2.assignRole(Seer())
        val p3 = Player("P3"); p3.assignRole(Medic())
        val p4 = Player("P4"); p4.assignRole(Villager())
        game.addPlayers(listOf(p1, p2, p3, p4))
        game.startGame() // This shuffles, but we manually set roles so it might override? 
        // startGame calls assignRoles which overwrites.
        // So we should construct game manually for specific scenarios or mock randomness?
        // Or just let it assign and find who is who.
        
        // Let's rely on finding players
        val wolf = game.players.first { it.role is Wolf }
        val seer = game.players.first { it.role is Seer }
        val medic = game.players.first { it.role is Medic }
        val villager = game.players.first { it.role is Villager } // If any
        
        assertEquals("Wolf", game.currentTurn)
        
        // Wolf kills
        game.handleNightAction(wolf, "KILL", medic.name)
        assertEquals("Seer", game.currentTurn)
        
        // Seer peeks
        game.handleNightAction(seer, "PEEK", wolf.name)
        assertEquals("Medic", game.currentTurn)
        
        // Medic heals (self or other)
        game.handleNightAction(medic, "HEAL", medic.name)
        
        // Night ends -> Day Discussion
        assertEquals(GamePhase.DAY_DISCUSSION, game.phase)
    }

    @Test
    fun `test voting results in death`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        val p1 = Player("P1"); p1.assignRole(Wolf())
        val p2 = Player("P2"); p2.assignRole(Villager())
        val p3 = Player("P3"); p3.assignRole(Villager())
        val p4 = Player("P4"); p4.assignRole(Villager())
        game.addPlayers(listOf(p1, p2, p3, p4))
        // Force state
        game.phase = GamePhase.DAY_VOTING
        
        game.handleVotingAction(p1, "VOTE", p2.name)
        game.handleVotingAction(p3, "VOTE", p2.name)
        game.handleVotingAction(p4, "VOTE", p2.name)
        game.handleVotingAction(p2, "VOTE", p1.name)
        
        // 3 votes for P2, 1 vote for P1. P2 dies.
        assertEquals(GamePhase.DAY_RESULTS, game.phase)
        assertEquals("P2", game.lastDeadPlayerName)
        assertFalse(p2.isAlive)
    }
    
    @Test
    fun `test win condition wolves win`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        val p1 = Player("P1"); p1.assignRole(Wolf())
        val p2 = Player("P2"); p2.assignRole(Villager())
        game.addPlayers(listOf(p1, p2))
        
        // 1 Wolf, 1 Villager -> Wolves Win (>=)
        assertTrue(game.checkWinCondition())
        assertEquals(GameStatus.FINISHED, game.status)
        assertEquals(Winner.WEREWOLVES, game.winner)
    }
    
    @Test
    fun `test win condition villagers win`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        val p1 = Player("P1"); p1.assignRole(Villager())
        val p2 = Player("P2"); p2.assignRole(Villager())
        val p3 = Player("P3"); p3.assignRole(Wolf()) // Dead wolf
        p3.die()
        game.addPlayers(listOf(p1, p2, p3))
        
        assertTrue(game.checkWinCondition())
        assertEquals(GameStatus.FINISHED, game.status)
        assertEquals(Winner.VILLAGERS, game.winner)
    }
}
