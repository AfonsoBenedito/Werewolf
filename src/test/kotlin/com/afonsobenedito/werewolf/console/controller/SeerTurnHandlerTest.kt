package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Medic
import com.afonsobenedito.werewolf.core.model.roles.Seer
import com.afonsobenedito.werewolf.core.model.roles.Villager
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeerTurnHandlerTest {

    private val handler = SeerTurnHandler()

    @Test
    fun `seer inspects wolf — announces NOT a regular Villager`() {
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }

        val allPlayers = listOf(seer, wolf)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(wolf)

        handler.handleTurn(listOf(seer), allPlayers, interaction)

        assertTrue(interaction.announcements.any { it.contains("NOT a regular Villager") })
    }

    @Test
    fun `seer inspects villager — announces regular Villager`() {
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val allPlayers = listOf(seer, villager)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(villager)

        handler.handleTurn(listOf(seer), allPlayers, interaction)

        assertTrue(interaction.announcements.any { it.contains("is a regular Villager") })
    }

    @Test
    fun `seer inspects medic — announces NOT a regular Villager`() {
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val allPlayers = listOf(seer, medic)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(medic)

        handler.handleTurn(listOf(seer), allPlayers, interaction)

        assertTrue(interaction.announcements.any { it.contains("NOT a regular Villager") })
    }

    @Test
    fun `seer skips — no role announcement`() {
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }

        val allPlayers = listOf(seer, wolf)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(null)

        handler.handleTurn(listOf(seer), allPlayers, interaction)

        assertTrue(interaction.announcements.none { it.contains("regular Villager") })
    }

    @Test
    fun `no active seer — announces silence`() {
        val villager = Player("Villager").apply { assignRole(Villager()) }

        val interaction = FakeGameInteraction(listOf(villager))

        handler.handleTurn(emptyList(), listOf(villager), interaction)

        assertTrue(interaction.announcements.any { it.contains("silent") })
    }

    @Test
    fun `seer always returns null — does not affect kill or save`() {
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }

        val allPlayers = listOf(seer, wolf)
        val interaction = FakeGameInteraction(allPlayers)
        interaction.queuePlayerSelection(wolf)

        val result = handler.handleTurn(listOf(seer), allPlayers, interaction)

        assertNull(result, "Seer turn should always return null")
    }
}
