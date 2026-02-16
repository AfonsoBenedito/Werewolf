package com.afonsobenedito.werewolf.console.runner

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.core.Winner
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Medic
import com.afonsobenedito.werewolf.core.model.roles.Seer
import com.afonsobenedito.werewolf.core.model.roles.Villager
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.testdouble.FakeGameInteraction
import com.afonsobenedito.werewolf.core.GameMode
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestableGame(id: String, name: String, mode: GameMode) : Game(id, name, mode) {
    override fun startGame() {
        // Bypass random assignment.
        // We assume roles are already set or will be set manually in the test setup.
        status = GameStatus.IN_PROGRESS
    }
}

class ConsoleGameRunnerTest {

    @Test
    fun `run game where Wolves win immediately (after night 1 kill)`() {
        // Setup scenarios: 
        // 3 Players: Wolf, Villager, Seer. 
        // Night 1: Wolf kills Villager. Seer checks Wolf.
        // Morning: Villager is dead.
        // Check Win Condition: 1 Wolf vs 1 Seer (Villager team). 1 >= 1. Wolves Win.
        
        val wolfPlayer = Player("WolfPlayer")
        val villagerPlayer = Player("VillagerPlayer")
        val seerPlayer = Player("SeerPlayer")
        
        wolfPlayer.assignRole(Wolf())
        villagerPlayer.assignRole(Villager())
        seerPlayer.assignRole(Seer())
        
        val players = listOf(wolfPlayer, villagerPlayer, seerPlayer)
        
        // Mock Interaction
        val fakeInteraction = FakeGameInteraction(players)
        
        // Wolf Turn: Select Villager to kill.
        fakeInteraction.queuePlayerSelection(villagerPlayer)
        
        // Seer Turn: Select Wolf to check.
        fakeInteraction.queuePlayerSelection(wolfPlayer)

        // Medic Turn: (No Medic in game, so no input needed)
        
        // Day 1: Voting.
        // If the game ends at morning check, we won't reach voting.
        // Game loop: 
        // 1. Night Phase (Wolf kill)
        // 2. checkWinCondition()
        // If win -> break.
        
        // So we expect the game to end after night 1.
        
        val game = TestableGame(UUID.randomUUID().toString(), "Test Game", GameMode.OFFLINE)
        val runner = ConsoleGameRunner(game, fakeInteraction)
        
        runner.run()
        
        assertTrue(villagerPlayer.isAlive == false, "Villager should be dead")
        assertTrue(game.status == GameStatus.FINISHED, "Game should be finished")
        assertEquals(Winner.WEREWOLVES, game.winner, "Wolves should win")
    }

    @Test
    fun `run game where Villagers win by voting`() {
        // Setup:
        // 3 Players: Wolf, Villager, Seer.
        // Night 1: Wolf tries to kill Villager.
        // Medic (add one) saves Villager? No let's keep it simple.
        // Let's say Wolf abstains or misses? Or Medic saves.
        // Let's add a Medic. 4 players. Wolf, Villager, Seer, Medic.
        
        // Night 1:
        // Wolf -> Kills Villager.
        // Medic -> Saves Villager.
        // Seer -> Checks Wolf (finds out they are a Wolf).
        
        // Day 1:
        // Morning -> No one died.
        // Voting -> Everyone votes for Wolf.
        // Wolf dies.
        // Win Condition: 0 Wolves. Villagers Win.
        
        val wolfPlayer = Player("WolfPlayer")
        val villagerPlayer = Player("VillagerPlayer")
        val seerPlayer = Player("SeerPlayer")
        val medicPlayer = Player("MedicPlayer")
        
        wolfPlayer.assignRole(Wolf())
        villagerPlayer.assignRole(Villager())
        seerPlayer.assignRole(Seer())
        medicPlayer.assignRole(Medic())
        
        val players = listOf(wolfPlayer, villagerPlayer, seerPlayer, medicPlayer)
        val fakeInteraction = FakeGameInteraction(players)
        
        // Night 1 Inputs:
        // Wolf Turn -> Kill Villager
        fakeInteraction.queuePlayerSelection(villagerPlayer)
        
        // Seer Turn -> Check Wolf
        fakeInteraction.queuePlayerSelection(wolfPlayer)

        // Medic Turn -> Save Villager
        fakeInteraction.queuePlayerSelection(villagerPlayer)
        
        // Day 1 Voting Inputs (4 players alive):
        // Wolf votes Villager
        fakeInteraction.queuePlayerSelection(villagerPlayer)
        // Villager votes Wolf
        fakeInteraction.queuePlayerSelection(wolfPlayer)
        // Seer votes Wolf
        fakeInteraction.queuePlayerSelection(wolfPlayer)
        // Medic votes Wolf
        fakeInteraction.queuePlayerSelection(wolfPlayer)
        
        // Result: Wolf has 3 votes, Villager 1. Wolf eliminated.
        
        val game = TestableGame(UUID.randomUUID().toString(), "Test Game", GameMode.OFFLINE)
        val runner = ConsoleGameRunner(game, fakeInteraction)
        
        runner.run()
        
        assertTrue(wolfPlayer.isAlive == false, "Wolf should be dead")
        assertTrue(villagerPlayer.isAlive == true, "Villager should be alive")
        assertEquals(Winner.VILLAGERS, game.winner, "Villagers should win")
    }
}
