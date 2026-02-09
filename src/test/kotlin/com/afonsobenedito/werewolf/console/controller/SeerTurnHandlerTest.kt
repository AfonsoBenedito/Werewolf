package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.model.Player
import com.afonsobenedito.werewolf.model.roles.Seer
import com.afonsobenedito.werewolf.model.roles.Villager
import com.afonsobenedito.werewolf.model.roles.Wolf
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import kotlin.test.Test
import kotlin.test.assertTrue

class SeerTurnHandlerTest {

    @Test
    fun `handleTurn announces correct role info`() {
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val activePlayers = listOf(seer)
        val allPlayers = listOf(seer, wolf, villager)
        
        val handler = SeerTurnHandler()
        val interaction = FakeGameInteraction(allPlayers)

        // Seer inspects Wolf
        interaction.queuePlayerSelection(wolf)

        handler.handleTurn(activePlayers, allPlayers, interaction)

        // Verify announcement contains "Agreed" or "Wolf" or whatever SeerTurnHandler outputs.
        // SeerTurnHandler output: "${selectedPlayer.name} is NOT a regular Villager!" (if hasPowers)
        // or "${selectedPlayer.name} is a regular Villager."
        
        val wolfAnnouncement = interaction.announcements.find { it.contains("NOT a regular Villager") }
        assertTrue(wolfAnnouncement != null, "Should announce Wolf is not a regular villager")
    }
}
