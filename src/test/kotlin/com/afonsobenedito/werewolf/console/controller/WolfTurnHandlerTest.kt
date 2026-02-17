package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Villager
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WolfTurnHandlerTest {

    private val handler = WolfTurnHandler()

    @Test
    fun `single wolf selects a target`() {
        val wolf = Player("Wolf1").apply { assignRole(Wolf()) }
        val victim = Player("Victim").apply { assignRole(Villager()) }

        val interaction = FakeGameInteraction(listOf(wolf, victim))
        interaction.queuePlayerSelection(victim)

        val result = handler.handleTurn(listOf(wolf), listOf(wolf, victim), interaction)

        assertEquals(victim, result)
        assertTrue(victim.isAlive, "Death is deferred — victim should still be alive")
    }

    @Test
    fun `two wolves agree on same target`() {
        val wolf1 = Player("Wolf1").apply { assignRole(Wolf()) }
        val wolf2 = Player("Wolf2").apply { assignRole(Wolf()) }
        val victim = Player("Victim").apply { assignRole(Villager()) }
        val other = Player("Other").apply { assignRole(Villager()) }

        val allPlayers = listOf(wolf1, wolf2, victim, other)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(victim)
        interaction.queuePlayerSelection(victim)

        val result = handler.handleTurn(listOf(wolf1, wolf2), allPlayers, interaction)

        assertEquals(victim, result)
    }

    @Test
    fun `two wolves disagree then agree on second round`() {
        val wolf1 = Player("Wolf1").apply { assignRole(Wolf()) }
        val wolf2 = Player("Wolf2").apply { assignRole(Wolf()) }
        val victim1 = Player("Victim1").apply { assignRole(Villager()) }
        val victim2 = Player("Victim2").apply { assignRole(Villager()) }

        val allPlayers = listOf(wolf1, wolf2, victim1, victim2)
        val interaction = FakeGameInteraction(allPlayers)

        // Round 1: disagree
        interaction.queuePlayerSelection(victim1)
        interaction.queuePlayerSelection(victim2)
        // Round 2: agree
        interaction.queuePlayerSelection(victim1)
        interaction.queuePlayerSelection(victim1)

        val result = handler.handleTurn(listOf(wolf1, wolf2), allPlayers, interaction)

        assertEquals(victim1, result)
        assertTrue(interaction.announcements.any { it.contains("divided") }, "Should announce wolves are divided")
    }

    @Test
    fun `single wolf abstains — no target`() {
        val wolf = Player("Wolf1").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val interaction = FakeGameInteraction(listOf(wolf, villager))
        interaction.queuePlayerSelection(null)

        val result = handler.handleTurn(listOf(wolf), listOf(wolf, villager), interaction)

        assertNull(result)
        assertTrue(villager.isAlive)
    }

    @Test
    fun `both wolves abstain — no target`() {
        val wolf1 = Player("Wolf1").apply { assignRole(Wolf()) }
        val wolf2 = Player("Wolf2").apply { assignRole(Wolf()) }
        val villager = Player("Victim").apply { assignRole(Villager()) }

        val allPlayers = listOf(wolf1, wolf2, villager)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        val result = handler.handleTurn(listOf(wolf1, wolf2), allPlayers, interaction)

        assertNull(result)
        assertTrue(interaction.announcements.any { it.contains("silent") }, "Should announce pack is silent")
    }

    @Test
    fun `no active wolves — announces silence`() {
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val interaction = FakeGameInteraction(listOf(villager))

        val result = handler.handleTurn(emptyList(), listOf(villager), interaction)

        assertNull(result)
        assertTrue(interaction.announcements.any { it.contains("silent") })
    }

    @Test
    fun `wolf targets are filtered to non-wolves`() {
        val wolf = Player("Wolf1").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val allPlayers = listOf(wolf, villager)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(villager)

        val result = handler.handleTurn(listOf(wolf), allPlayers, interaction)

        assertEquals(villager, result, "Wolf should be able to target non-wolf player")
    }
}
