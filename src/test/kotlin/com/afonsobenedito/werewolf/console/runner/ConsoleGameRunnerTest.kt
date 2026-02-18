package com.afonsobenedito.werewolf.console.runner

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.core.Winner
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Medic
import com.afonsobenedito.werewolf.core.model.roles.Seer
import com.afonsobenedito.werewolf.core.model.roles.Villager
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestableGame(id: String, name: String, mode: GameMode) : Game(id, name, mode) {
    override fun startGame() {
        status = GameStatus.IN_PROGRESS
    }
}

class ConsoleGameRunnerTest {

    private fun createGame() = TestableGame(UUID.randomUUID().toString(), "Test Game", GameMode.OFFLINE)


    @Test
    fun `wolves win after night 1 kill — 3 players`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }

        val players = listOf(wolf, villager, seer)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertFalse(villager.isAlive)
        assertEquals(GameStatus.FINISHED, game.status)
        assertEquals(Winner.WEREWOLVES, game.winner)
    }


    @Test
    fun `villagers win by voting out wolf after medic save`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(villager)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertFalse(wolf.isAlive)
        assertTrue(villager.isAlive)
        assertEquals(Winner.VILLAGERS, game.winner)
    }


    @Test
    fun `tie vote results in no elimination — game continues`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager1 = Player("Villager1").apply { assignRole(Villager()) }
        val villager2 = Player("Villager2").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }

        val players = listOf(wolf, villager1, villager2, seer)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(wolf)

        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(wolf)

        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(villager2)

        interaction.queuePlayerSelection(seer)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertFalse(wolf.isAlive)
        assertFalse(villager1.isAlive)
        assertTrue(villager2.isAlive)
        assertTrue(seer.isAlive)
        assertEquals(Winner.VILLAGERS, game.winner)
    }


    @Test
    fun `wolf abstains at night — no one dies, game continues to day`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertFalse(wolf.isAlive)
        assertTrue(villager.isAlive)
        assertEquals(Winner.VILLAGERS, game.winner)
    }


    @Test
    fun `multi-round game — wolves win after two nights`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager1 = Player("Villager1").apply { assignRole(Villager()) }
        val villager2 = Player("Villager2").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager1, villager2, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(villager2)
        interaction.queuePlayerSelection(null)

        interaction.queuePlayerSelection(seer)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(seer)

        interaction.queuePlayerSelection(seer)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(null)

        interaction.queuePlayerSelection(villager2)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(villager2)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertEquals(GameStatus.FINISHED, game.status)
        assertEquals(Winner.WEREWOLVES, game.winner)
        assertFalse(villager2.isAlive)
        assertFalse(seer.isAlive)
    }


    @Test
    fun `game announces werewolves win`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }

        val players = listOf(wolf, villager, seer)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertTrue(interaction.announcements.any { it.contains("WEREWOLVES WIN") })
    }

    @Test
    fun `game announces villagers win`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(villager)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertTrue(interaction.announcements.any { it.contains("VILLAGERS WIN") })
    }

    @Test
    fun `morning report announces death`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }

        val players = listOf(wolf, villager, seer)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertTrue(interaction.announcements.any { it.contains("Villager") && it.contains("died") })
    }

    @Test
    fun `morning report announces peaceful night when wolf abstains`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertTrue(interaction.announcements.any { it.contains("peaceful night") })
    }

    @Test
    fun `morning report announces peaceful night when medic saves target`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(villager)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertTrue(interaction.announcements.any { it.contains("peaceful night") })
        assertTrue(villager.isAlive, "Villager should be saved by medic")
    }

    @Test
    fun `day vote announces elimination`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertTrue(interaction.announcements.any { it.contains("Wolf") && it.contains("eliminated") })
    }

    @Test
    fun `all players skip vote — no elimination`() {
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        interaction.queuePlayerSelection(seer)
        interaction.queuePlayerSelection(null)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertEquals(Winner.WEREWOLVES, game.winner)
    }
}
