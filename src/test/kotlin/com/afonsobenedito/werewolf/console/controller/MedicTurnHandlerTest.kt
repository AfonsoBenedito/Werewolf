package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Medic
import com.afonsobenedito.werewolf.core.model.roles.Villager
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MedicTurnHandlerTest {

    private val handler = MedicTurnHandler()

    @Test
    fun `medic heals dead player — player is resurrected`() {
        val medic = Player("Medic").apply { assignRole(Medic()) }
        val victim = Player("Victim").apply { assignRole(Villager()) }
        victim.die()

        val allPlayers = listOf(medic, victim)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(victim)

        handler.handleTurn(listOf(medic), allPlayers, interaction)

        assertTrue(victim.isAlive, "Victim should be resurrected")
    }

    @Test
    fun `medic returns selected player`() {
        val medic = Player("Medic").apply { assignRole(Medic()) }
        val target = Player("Target").apply { assignRole(Villager()) }

        val allPlayers = listOf(medic, target)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(target)

        val result = handler.handleTurn(listOf(medic), allPlayers, interaction)

        assertEquals(target, result, "Should return the protected player")
    }

    @Test
    fun `medic skips — returns null`() {
        val medic = Player("Medic").apply { assignRole(Medic()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val allPlayers = listOf(medic, villager)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(null)

        val result = handler.handleTurn(listOf(medic), allPlayers, interaction)

        assertNull(result, "Should return null when medic skips")
    }

    @Test
    fun `no active medic — announces silence and returns null`() {
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val interaction = FakeGameInteraction(listOf(villager))

        val result = handler.handleTurn(emptyList(), listOf(villager), interaction)

        assertNull(result)
        assertTrue(interaction.announcements.any { it.contains("silent") })
    }

    @Test
    fun `medic protects alive player — player stays alive`() {
        val medic = Player("Medic").apply { assignRole(Medic()) }
        val target = Player("Target").apply { assignRole(Villager()) }

        val allPlayers = listOf(medic, target)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(target)

        handler.handleTurn(listOf(medic), allPlayers, interaction)

        assertTrue(target.isAlive)
    }
}
