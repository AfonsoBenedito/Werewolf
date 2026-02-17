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

    // --- Werewolves Win Scenarios ---

    @Test
    fun `wolves win after night 1 kill — 3 players`() {
        // Wolf kills Villager. 1 Wolf vs 1 Seer → wolves win.
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }

        val players = listOf(wolf, villager, seer)
        val interaction = FakeGameInteraction(players)

        // Night: Wolf kills Villager
        interaction.queuePlayerSelection(villager)
        // Night: Seer checks Wolf
        interaction.queuePlayerSelection(wolf)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertFalse(villager.isAlive)
        assertEquals(GameStatus.FINISHED, game.status)
        assertEquals(Winner.WEREWOLVES, game.winner)
    }

    // --- Villagers Win Scenarios ---

    @Test
    fun `villagers win by voting out wolf after medic save`() {
        // Night: Wolf kills Villager, Medic saves Villager, Seer checks Wolf.
        // Day: Everyone votes Wolf. Villagers win.
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        // Night 1: Wolf → Villager
        interaction.queuePlayerSelection(villager)
        // Night 1: Seer → Wolf
        interaction.queuePlayerSelection(wolf)
        // Night 1: Medic → Villager (save)
        interaction.queuePlayerSelection(villager)

        // Day 1 Voting (4 alive): Wolf→Villager, Villager→Wolf, Seer→Wolf, Medic→Wolf
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

    // --- Tie Vote Scenarios ---

    @Test
    fun `tie vote results in no elimination — game continues`() {
        // 4 players: Wolf, Villager1, Villager2, Seer.
        // Night 1: Wolf abstains. Seer checks Wolf.
        // Day 1: 2 vote Wolf, 2 vote Villager1 → tie, no elimination.
        // Night 2: Wolf kills Villager1. Seer checks Villager2.
        // Morning: Villager1 dead. 1 Wolf vs 2 (Seer + Villager2). Game continues.
        // Day 2: Seer and Villager2 vote Wolf. Wolf votes Seer.
        // Wolf eliminated. Villagers win.
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager1 = Player("Villager1").apply { assignRole(Villager()) }
        val villager2 = Player("Villager2").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }

        val players = listOf(wolf, villager1, villager2, seer)
        val interaction = FakeGameInteraction(players)

        // Night 1: Wolf abstains, Seer checks Wolf
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(wolf)

        // Day 1 Voting (4 alive): tie — Wolf→Villager1, Villager1→Wolf, Villager2→Villager1, Seer→Wolf
        // Wolf: 2 votes, Villager1: 2 votes → tie
        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(wolf)

        // Night 2: Wolf kills Villager1, Seer checks Villager2
        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(villager2)

        // Day 2 Voting (3 alive: Wolf, Villager2, Seer):
        // Wolf→Seer, Villager2→Wolf, Seer→Wolf
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

    // --- Wolf Abstain Scenarios ---

    @Test
    fun `wolf abstains at night — no one dies, game continues to day`() {
        // 4 players. Wolf abstains. No one dies overnight.
        // Day: all vote Wolf. Villagers win.
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        // Night 1: Wolf abstains, Seer skips, Medic skips
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        // Day 1: everyone votes Wolf
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

    // --- Multi-Round Scenarios ---

    @Test
    fun `multi-round game — wolves win after two nights`() {
        // 5 players: Wolf, Villager1, Villager2, Seer, Medic.
        // Night 1: Wolf kills Villager1, Seer checks Villager2, Medic skips.
        // Day 1: Tie vote, no elimination.
        // Night 2: Wolf kills Seer.
        // Morning: Seer dead. 1 Wolf vs 2 (Villager2 + Medic). Game continues.
        // Day 2: Wolf→Villager2, Villager2→Wolf, Medic→Villager2. Villager2 eliminated.
        // 1 Wolf vs 1 Medic. Wolves win.
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager1 = Player("Villager1").apply { assignRole(Villager()) }
        val villager2 = Player("Villager2").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager1, villager2, seer, medic)
        val interaction = FakeGameInteraction(players)

        // Night 1: Wolf→Villager1, Seer→Villager2, Medic skips
        interaction.queuePlayerSelection(villager1)
        interaction.queuePlayerSelection(villager2)
        interaction.queuePlayerSelection(null)

        // Day 1 Voting (4 alive: Wolf, Villager2, Seer, Medic):
        // Tie: Wolf→Seer, Villager2→Wolf, Seer→Wolf, Medic→Seer
        interaction.queuePlayerSelection(seer)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(seer)

        // Night 2: Wolf→Seer, Seer checks Wolf (still alive), Medic skips
        interaction.queuePlayerSelection(seer)
        interaction.queuePlayerSelection(wolf)
        interaction.queuePlayerSelection(null)

        // Day 2 Voting (3 alive: Wolf, Villager2, Medic):
        // Wolf→Villager2, Villager2→Wolf, Medic→Villager2
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

    // --- Announcement Verification ---

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

        // Night: Wolf→Villager, Seer skips, Medic saves Villager
        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(villager)

        // Day: all vote Wolf
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

        // Night: Wolf abstains, Seer skips, Medic skips
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        // Day: all vote Wolf
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

        // Night: Wolf→Villager, Seer skips, Medic→Villager (save)
        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(villager)

        // Day: all vote Wolf
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

        // Night: all skip
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        // Day: all vote Wolf
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
        // 4 players. Night: Wolf kills Villager.
        // After night: 1 Wolf vs 2 (Seer + Medic). Not enough for wolves to win yet.
        // Day: all skip. No elimination.
        // Night 2: Wolf kills Seer.
        // 1 Wolf vs 1 Medic → Wolves win.
        val wolf = Player("Wolf").apply { assignRole(Wolf()) }
        val villager = Player("Villager").apply { assignRole(Villager()) }
        val seer = Player("Seer").apply { assignRole(Seer()) }
        val medic = Player("Medic").apply { assignRole(Medic()) }

        val players = listOf(wolf, villager, seer, medic)
        val interaction = FakeGameInteraction(players)

        // Night 1: Wolf→Villager, Seer skips, Medic skips
        interaction.queuePlayerSelection(villager)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        // Day 1: all skip (3 alive: Wolf, Seer, Medic)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)
        interaction.queuePlayerSelection(null)

        // Night 2: Wolf→Seer, Medic skips (Seer is dead after night resolves so no seer turn)
        interaction.queuePlayerSelection(seer)
        interaction.queuePlayerSelection(null)

        val game = createGame()
        ConsoleGameRunner(game, interaction).run()

        assertEquals(Winner.WEREWOLVES, game.winner)
    }
}
