package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.model.Player
import com.afonsobenedito.werewolf.model.roles.Villager
import com.afonsobenedito.werewolf.model.roles.Wolf
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WolfTurnHandlerTest {

    @Test
    fun `handleTurn kills victim if wolves agree`() {
        val wolf1 = Player("Wolf1").apply { assignRole(Wolf()) }
        val wolf2 = Player("Wolf2").apply { assignRole(Wolf()) }
        val victim = Player("Victim").apply { assignRole(Villager()) }
        val other = Player("Other").apply { assignRole(Villager()) }

        // activePlayers = wolves
        val activePlayers = listOf(wolf1, wolf2)
        // allPlayers = all
        val allPlayers = listOf(wolf1, wolf2, victim, other)
        
        val handler = WolfTurnHandler()
        val interaction = FakeGameInteraction(allPlayers)

        // Wolf1 chooses Victim
        interaction.queuePlayerSelection(victim)
        // Wolf2 chooses Victim
        interaction.queuePlayerSelection(victim)

        handler.handleTurn(activePlayers, allPlayers, interaction)

        assertFalse(victim.isAlive, "Victim should be dead")
    }

    @Test
    fun `handleTurn does nothing if wolves abstain`() {
        val wolf1 = Player("Wolf1").apply { assignRole(Wolf()) }
        val victim = Player("Victim").apply { assignRole(Villager()) }

        val activePlayers = listOf(wolf1)
        val allPlayers = listOf(wolf1, victim)
        
        val handler = WolfTurnHandler()
        val interaction = FakeGameInteraction(allPlayers)

        // Wolf1 abstains (returns null)
        interaction.queuePlayerSelection(null)

        handler.handleTurn(activePlayers, allPlayers, interaction)

        assertTrue(victim.isAlive, "Victim should still be alive")
    }
}
