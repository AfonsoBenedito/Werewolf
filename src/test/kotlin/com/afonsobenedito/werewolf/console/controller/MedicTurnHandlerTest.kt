package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Medic
import com.afonsobenedito.werewolf.core.model.roles.Villager
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import kotlin.test.Test
import kotlin.test.assertTrue

class MedicTurnHandlerTest {

    @Test
    fun `handleTurn heals selected target`() {
        val medic = Player("Medic").apply { assignRole(Medic()) }
        val victim = Player("Victim").apply { assignRole(Villager()) }
        victim.die() // Victim is dead

        val activePlayers = listOf(medic)
        val allPlayers = listOf(medic, victim)
        
        val handler = MedicTurnHandler()
        val interaction = FakeGameInteraction(allPlayers)

        // Medic selects Victim to heal
        interaction.queuePlayerSelection(victim)

        handler.handleTurn(activePlayers, allPlayers, interaction)

        assertTrue(victim.isAlive, "Victim should be resurrected")
    }
}
