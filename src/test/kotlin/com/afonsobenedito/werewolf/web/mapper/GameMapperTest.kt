package com.afonsobenedito.werewolf.web.mapper

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.core.GamePhase
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameMapperTest {

    private val mapper = GameMapper()

    @Test
    fun `toGameResponse masks votes during voting phase`() {
        val game = Game("id", "name", GameMode.ONLINE)
        game.phase = GamePhase.DAY_VOTING
        game.votes["P1"] = "P2"
        game.votes["P2"] = "P1"
        
        val responseP1 = mapper.toGameResponse(game, "P1")
        assertEquals("P2", responseP1.votes["P1"])
        assertEquals("SECRET", responseP1.votes["P2"])
        
        val responseP2 = mapper.toGameResponse(game, "P2")
        assertEquals("P1", responseP2.votes["P2"])
        assertEquals("SECRET", responseP2.votes["P1"])
    }

    @Test
    fun `toGameResponse reveals roles to self`() {
        val game = Game("id", "name", GameMode.ONLINE)
        val p1 = Player("P1"); p1.assignRole(Wolf())
        game.addPlayers(listOf(p1))
        
        val response = mapper.toGameResponse(game, "P1")
        assertEquals("Werewolf", response.players.find { it.name == "P1" }?.role)
    }

    @Test
    fun `toGameResponse hides roles from others`() {
        val game = Game("id", "name", GameMode.ONLINE)
        val p1 = Player("P1"); p1.assignRole(Wolf())
        val p2 = Player("P2") // Villager/Unknown
        game.addPlayers(listOf(p1, p2))
        
        val response = mapper.toGameResponse(game, "P2")
        assertEquals("Unknown", response.players.find { it.name == "P1" }?.role)
    }
    
    @Test
    fun `toGameResponse reveals wolf role to other wolf`() {
        val game = Game("id", "name", GameMode.ONLINE)
        val p1 = Player("P1"); p1.assignRole(Wolf())
        val p2 = Player("P2"); p2.assignRole(Wolf())
        game.addPlayers(listOf(p1, p2))
        
        val response = mapper.toGameResponse(game, "P1")
        assertEquals("Werewolf", response.players.find { it.name == "P2" }?.role)
    }
}
