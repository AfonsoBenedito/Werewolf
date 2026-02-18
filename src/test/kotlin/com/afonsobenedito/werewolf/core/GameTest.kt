package com.afonsobenedito.werewolf.core

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GameTest {


    /**
     * Creates a game in NIGHT phase with known roles, bypassing the random shuffle of startGame().
     * Default setup: 1 Wolf, 1 Seer, 1 Medic, 1 Villager.
     */
    private fun createNightGame(
        playerRoles: List<Pair<String, Role>> = listOf(
            "Wolf1" to Wolf(),
            "Seer1" to Seer(),
            "Medic1" to Medic(),
            "Villager1" to Villager()
        )
    ): Game {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        playerRoles.forEach { (name, role) ->
            val player = Player(name)
            player.assignRole(role)
            game.addPlayer(player)
        }
        game.phase = GamePhase.NIGHT
        game.dayCount = 1
        game.startNightPhase()
        return game
    }

    private fun Game.findPlayer(name: String) = players.first { it.name == name }


    @Test
    fun `initial game state`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        assertEquals("test-id", game.id)
        assertEquals("Test Game", game.name)
        assertEquals(GameMode.ONLINE, game.mode)
        assertEquals(GameStatus.NOT_STARTED, game.status)
        assertEquals(GamePhase.NIGHT, game.phase)
        assertEquals(0, game.dayCount)
        assertNull(game.winner)
        assertTrue(game.players.isEmpty())
    }


    @Test
    fun `startGame with 4 players assigns 1 wolf`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        game.addPlayers((1..4).map { Player("P$it") })
        game.startGame()

        assertEquals(1, game.players.count { it.role is Wolf })
        assertEquals(1, game.players.count { it.role is Seer })
        assertEquals(1, game.players.count { it.role is Medic })
        assertEquals(1, game.players.count { it.role is Villager })
        assertEquals(GameStatus.IN_PROGRESS, game.status)
        assertEquals(GamePhase.NIGHT, game.phase)
        assertEquals(1, game.dayCount)
        assertEquals("Wolf", game.currentTurn)
    }

    @Test
    fun `startGame with 6 players assigns 2 wolves`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        game.addPlayers((1..6).map { Player("P$it") })
        game.startGame()

        assertEquals(2, game.players.count { it.role is Wolf })
        assertEquals(1, game.players.count { it.role is Seer })
        assertEquals(1, game.players.count { it.role is Medic })
        assertEquals(2, game.players.count { it.role is Villager })
    }

    @Test
    fun `startGame with fewer than 4 players throws`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        game.addPlayers(listOf(Player("P1"), Player("P2"), Player("P3")))

        val ex = assertThrows<IllegalArgumentException> { game.startGame() }
        assertEquals("Need at least 4 players to start a game", ex.message)
    }


    @Test
    fun `wrong role acting throws`() {
        val game = createNightGame()
        val seer = game.findPlayer("Seer1")
        assertEquals("Wolf", game.currentTurn)

        val ex = assertThrows<IllegalArgumentException> {
            game.handleNightAction(seer, "PEEK", "Wolf1")
        }
        assertTrue(ex.message!!.contains("It is not your turn"))
    }

    @Test
    fun `invalid action type throws`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")

        assertThrows<IllegalArgumentException> {
            game.handleNightAction(wolf, "INVALID", "Villager1")
        }
    }


    @Test
    fun `wolf kill advances to seer turn`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")

        game.handleNightAction(wolf, "KILL", "Villager1")
        assertEquals("Seer", game.currentTurn)
        assertEquals("Villager1", game.pendingDeathId)
    }

    @Test
    fun `wolf cannot kill another wolf`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager())
        )
        val w1 = game.findPlayer("W1")

        assertThrows<IllegalArgumentException> {
            game.handleNightAction(w1, "KILL", "W2")
        }
    }

    @Test
    fun `wolf skip sets no target`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")

        game.handleNightAction(wolf, "SKIP", null)
        assertEquals("Seer", game.currentTurn)
        assertNull(game.pendingDeathId)
    }

    @Test
    fun `two wolves must agree on target`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager())
        )
        val w1 = game.findPlayer("W1")
        val w2 = game.findPlayer("W2")

        val result1 = game.handleNightAction(w1, "KILL", "V1")
        assertEquals("Waiting for other werewolf...", result1)
        assertEquals("Wolf", game.currentTurn)

        val result2 = game.handleNightAction(w2, "KILL", "Seer1")
        assertEquals("Wolves have selected different targets! You must agree.", result2)
        assertEquals("Wolf", game.currentTurn)
    }

    @Test
    fun `two wolves agree on target advances turn`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager())
        )
        val w1 = game.findPlayer("W1")
        val w2 = game.findPlayer("W2")

        game.handleNightAction(w1, "KILL", "V1")
        game.handleNightAction(w2, "KILL", "V1")

        assertEquals("Seer", game.currentTurn)
        assertEquals("V1", game.pendingDeathId)
    }

    @Test
    fun `two wolves both skip sets no target`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager())
        )
        val w1 = game.findPlayer("W1")
        val w2 = game.findPlayer("W2")

        game.handleNightAction(w1, "SKIP", null)
        game.handleNightAction(w2, "SKIP", null)

        assertEquals("Seer", game.currentTurn)
        assertNull(game.pendingDeathId)
    }


    @Test
    fun `seer peek on villager returns villager message`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")

        game.handleNightAction(wolf, "KILL", "Villager1")

        val result = game.handleNightAction(seer, "PEEK", "Villager1")
        assertEquals("Villager1 is just a Regular Villager", result)
    }

    @Test
    fun `seer peek on wolf returns not villager message`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")

        game.handleNightAction(wolf, "KILL", "Villager1")

        val result = game.handleNightAction(seer, "PEEK", "Wolf1")
        assertEquals("Wolf1 is not just a Regular Villager", result)
    }

    @Test
    fun `seer cannot peek twice`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")

        game.handleNightAction(wolf, "KILL", "Villager1")
        game.handleNightAction(seer, "PEEK", "Wolf1")

        assertThrows<IllegalArgumentException> {
            game.handleNightAction(seer, "PEEK", "Villager1")
        }
    }

    @Test
    fun `seer skip advances to medic`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")

        game.handleNightAction(wolf, "KILL", "Villager1")
        game.handleNightAction(seer, "SKIP", null)

        assertEquals("Medic", game.currentTurn)
    }


    @Test
    fun `medic heals pending death target`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")
        val medic = game.findPlayer("Medic1")

        game.handleNightAction(wolf, "KILL", "Villager1")
        game.handleNightAction(seer, "SKIP", null)

        assertEquals("Villager1", game.pendingDeathId)
        game.handleNightAction(medic, "HEAL", "Villager1")

        assertNull(game.pendingDeathId)
        assertEquals(GamePhase.DAY_DISCUSSION, game.phase)
    }

    @Test
    fun `medic heals wrong target does not clear pending death`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")
        val medic = game.findPlayer("Medic1")

        game.handleNightAction(wolf, "KILL", "Villager1")
        game.handleNightAction(seer, "SKIP", null)
        game.handleNightAction(medic, "HEAL", "Seer1")

        assertEquals(GamePhase.DAY_DISCUSSION, game.phase)
        assertFalse(game.findPlayer("Villager1").isAlive)
        assertEquals("Villager1", game.lastDeadPlayerName)
    }

    @Test
    fun `medic skip does not heal anyone`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")
        val medic = game.findPlayer("Medic1")

        game.handleNightAction(wolf, "KILL", "Villager1")
        game.handleNightAction(seer, "SKIP", null)
        game.handleNightAction(medic, "SKIP", null)

        assertEquals(GamePhase.DAY_DISCUSSION, game.phase)
        assertFalse(game.findPlayer("Villager1").isAlive)
    }


    @Test
    fun `full night with no kill results in peaceful day`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        val seer = game.findPlayer("Seer1")
        val medic = game.findPlayer("Medic1")

        game.handleNightAction(wolf, "SKIP", null)
        game.handleNightAction(seer, "SKIP", null)
        game.handleNightAction(medic, "SKIP", null)

        assertEquals(GamePhase.DAY_DISCUSSION, game.phase)
        assertNull(game.lastDeadPlayerName)
        assertTrue(game.players.all { it.isAlive })
    }

    @Test
    fun `night skips dead roles`() {
        val game = createNightGame()
        game.findPlayer("Seer1").die()
        game.startNightPhase()

        val wolf = game.findPlayer("Wolf1")
        game.handleNightAction(wolf, "KILL", "Villager1")

        assertEquals("Medic", game.currentTurn)
    }

    @Test
    fun `night with no alive special roles advances to day`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager(), "V2" to Villager())
        )
        game.findPlayer("Seer1").die()
        game.findPlayer("Medic1").die()
        game.startNightPhase()

        val wolf = game.findPlayer("W1")
        game.handleNightAction(wolf, "SKIP", null)

        assertEquals(GamePhase.DAY_DISCUSSION, game.phase)
    }


    @Test
    fun `voting results in death of majority target`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        val p1 = game.findPlayer("Wolf1")
        val p2 = game.findPlayer("Seer1")
        val p3 = game.findPlayer("Medic1")
        val p4 = game.findPlayer("Villager1")

        game.handleVotingAction(p1, "VOTE", "Seer1")
        game.handleVotingAction(p2, "VOTE", "Wolf1")
        game.handleVotingAction(p3, "VOTE", "Seer1")
        game.handleVotingAction(p4, "VOTE", "Seer1")

        assertEquals(GamePhase.DAY_RESULTS, game.phase)
        assertEquals("Seer1", game.lastDeadPlayerName)
        assertFalse(p2.isAlive)
    }

    @Test
    fun `tie vote results in no elimination`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        val p1 = game.findPlayer("Wolf1")
        val p2 = game.findPlayer("Seer1")
        val p3 = game.findPlayer("Medic1")
        val p4 = game.findPlayer("Villager1")

        game.handleVotingAction(p1, "VOTE", "Seer1")
        game.handleVotingAction(p2, "VOTE", "Wolf1")
        game.handleVotingAction(p3, "VOTE", "Wolf1")
        game.handleVotingAction(p4, "VOTE", "Seer1")

        assertEquals(GamePhase.DAY_RESULTS, game.phase)
        assertNull(game.lastDeadPlayerName)
        assertTrue(game.players.all { it.isAlive })
    }

    @Test
    fun `unvote removes vote`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        val p1 = game.findPlayer("Wolf1")

        game.handleVotingAction(p1, "VOTE", "Seer1")
        assertEquals("Seer1", game.votes["Wolf1"])

        game.handleVotingAction(p1, "UNVOTE", null)
        assertFalse(game.votes.containsKey("Wolf1"))
    }

    @Test
    fun `skip vote counts as abstain`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        val p1 = game.findPlayer("Wolf1")

        game.handleVotingAction(p1, "SKIP", null)
        assertEquals("ABSTAIN", game.votes["Wolf1"])
    }

    @Test
    fun `all players abstain results in no elimination`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        game.players.filter { it.isAlive }.forEach {
            game.handleVotingAction(it, "SKIP", null)
        }

        assertEquals(GamePhase.DAY_RESULTS, game.phase)
        assertNull(game.lastDeadPlayerName)
    }

    @Test
    fun `vote for dead player throws`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING
        game.findPlayer("Seer1").die()

        assertThrows<IllegalArgumentException> {
            game.handleVotingAction(game.findPlayer("Wolf1"), "VOTE", "Seer1")
        }
    }

    @Test
    fun `vote without target throws`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        assertThrows<IllegalArgumentException> {
            game.handleVotingAction(game.findPlayer("Wolf1"), "VOTE", null)
        }
    }

    @Test
    fun `vote for nonexistent player throws`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        assertThrows<IllegalArgumentException> {
            game.handleVotingAction(game.findPlayer("Wolf1"), "VOTE", "Nobody")
        }
    }

    @Test
    fun `invalid voting action throws`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        assertThrows<IllegalArgumentException> {
            game.handleVotingAction(game.findPlayer("Wolf1"), "INVALID", null)
        }
    }


    @Test
    fun `day discussion advances to day voting`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_DISCUSSION

        game.advancePhase()

        assertEquals(GamePhase.DAY_VOTING, game.phase)
    }

    @Test
    fun `day results advances to next night and increments dayCount`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_RESULTS
        val previousDay = game.dayCount

        game.advancePhase()

        assertEquals(GamePhase.NIGHT, game.phase)
        assertEquals(previousDay + 1, game.dayCount)
        assertEquals("Wolf", game.currentTurn)
        assertNull(game.lastDeadPlayerName)
    }

    @Test
    fun `day results resets night state`() {
        val game = createNightGame()
        game.wolfVotes["Wolf1"] = "Villager1"

        game.phase = GamePhase.DAY_RESULTS
        game.advancePhase()

        assertTrue(game.wolfVotes.isEmpty())
        assertFalse(game.seerHasPeeked)
    }


    @Test
    fun `villagers win when all wolves are dead`() {
        val game = createNightGame()
        game.findPlayer("Wolf1").die()

        assertTrue(game.evaluateWinCondition())
        assertEquals(GameStatus.FINISHED, game.status)
        assertEquals(Winner.VILLAGERS, game.winner)
    }

    @Test
    fun `wolves win when they equal villagers`() {
        val game = createNightGame()
        game.findPlayer("Seer1").die()
        game.findPlayer("Medic1").die()

        assertTrue(game.evaluateWinCondition())
        assertEquals(GameStatus.FINISHED, game.status)
        assertEquals(Winner.WEREWOLVES, game.winner)
    }

    @Test
    fun `wolves win when they outnumber villagers`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager())
        )
        game.findPlayer("Seer1").die()
        game.findPlayer("Medic1").die()

        assertTrue(game.evaluateWinCondition())
        assertEquals(Winner.WEREWOLVES, game.winner)
    }

    @Test
    fun `no win condition when game is still balanced`() {
        val game = createNightGame()

        assertFalse(game.evaluateWinCondition())
        assertEquals(GameStatus.NOT_STARTED, game.status)
        assertNull(game.winner)
    }

    @Test
    fun `checkPhaseTransition sets FINISHED when win condition met`() {
        val game = createNightGame()
        game.findPlayer("Wolf1").die()

        game.checkPhaseTransition()

        assertEquals(GamePhase.FINISHED, game.phase)
        assertEquals(GameStatus.FINISHED, game.status)
    }

    @Test
    fun `checkPhaseTransition does nothing when no winner`() {
        val game = createNightGame()

        game.checkPhaseTransition()

        assertEquals(GamePhase.NIGHT, game.phase)
    }

    @Test
    fun `voting that causes wolf elimination triggers villager win`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        game.handleVotingAction(game.findPlayer("Seer1"), "VOTE", "Wolf1")
        game.handleVotingAction(game.findPlayer("Medic1"), "VOTE", "Wolf1")
        game.handleVotingAction(game.findPlayer("Villager1"), "VOTE", "Wolf1")
        game.handleVotingAction(game.findPlayer("Wolf1"), "VOTE", "Seer1")

        assertEquals(GamePhase.FINISHED, game.phase)
        assertEquals(Winner.VILLAGERS, game.winner)
    }


    @Test
    fun `offline KILL action sets pending death`() {
        val game = createNightGame()

        game.processOfflineAction("KILL", "Master", "Villager1")

        assertEquals("Villager1", game.pendingDeathId)
    }

    @Test
    fun `offline HEAL action clears pending death`() {
        val game = createNightGame()
        game.processOfflineAction("KILL", "Master", "Villager1")
        game.processOfflineAction("NEXT_TURN", "Master", null)

        game.processOfflineAction("HEAL", "Master", "Villager1")

        assertNull(game.pendingDeathId)
    }

    @Test
    fun `offline PEEK action returns role hint`() {
        val game = createNightGame()
        game.processOfflineAction("KILL", "Master", "Villager1")

        val villagerResult = game.processOfflineAction("PEEK", "Master", "Villager1")
        assertEquals("Villager", villagerResult)
    }

    @Test
    fun `offline PEEK on wolf returns NOT a Villager`() {
        val game = createNightGame()
        game.processOfflineAction("KILL", "Master", "Villager1")

        val wolfResult = game.processOfflineAction("PEEK", "Master", "Wolf1")
        assertEquals("NOT a Villager", wolfResult)
    }

    @Test
    fun `offline NEXT_TURN advances turn`() {
        val game = createNightGame()
        game.processOfflineAction("KILL", "Master", "Villager1")
        game.processOfflineAction("NEXT_TURN", "Master", null)

        assertEquals("Medic", game.currentTurn)
    }

    @Test
    fun `offline NEXT_PHASE advances phase`() {
        val game = createNightGame()
        game.processOfflineAction("NEXT_PHASE", "Master", null)

        assertEquals(GamePhase.DAY_DISCUSSION, game.phase)
    }

    @Test
    fun `offline ELIMINATE kills player and starts new night`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        game.processOfflineAction("ELIMINATE", "Master", "Villager1")

        assertFalse(game.findPlayer("Villager1").isAlive)
        assertEquals(GamePhase.NIGHT, game.phase)
        assertEquals(2, game.dayCount)
        assertEquals("Wolf", game.currentTurn)
    }

    @Test
    fun `offline ELIMINATE triggering win sets FINISHED`() {
        val game = createNightGame()
        game.findPlayer("Seer1").die()
        game.findPlayer("Medic1").die()
        game.phase = GamePhase.DAY_VOTING

        game.processOfflineAction("ELIMINATE", "Master", "Villager1")

        assertEquals(GamePhase.FINISHED, game.phase)
        assertEquals(Winner.WEREWOLVES, game.winner)
    }

    @Test
    fun `offline VOTE from Master is ignored`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        val result = game.processOfflineAction("VOTE", "Master", "Wolf1")

        assertNull(result)
        assertTrue(game.votes.isEmpty())
    }

    @Test
    fun `offline VOTE from player registers vote`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        game.processOfflineAction("VOTE", "Wolf1", "Seer1")

        assertEquals("Seer1", game.votes["Wolf1"])
    }

    @Test
    fun `offline VOTE SKIP registers abstain`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_VOTING

        game.processOfflineAction("VOTE", "Wolf1", "SKIP")

        assertEquals("ABSTAIN", game.votes["Wolf1"])
    }


    @Test
    fun `getWolfStatus returns null when not wolf turn`() {
        val game = createNightGame()
        val wolf = game.findPlayer("Wolf1")
        game.handleNightAction(wolf, "KILL", "Villager1")

        assertNull(game.getWolfStatus())
    }

    @Test
    fun `getWolfStatus returns null during day`() {
        val game = createNightGame()
        game.phase = GamePhase.DAY_DISCUSSION

        assertNull(game.getWolfStatus())
    }

    @Test
    fun `getWolfStatus returns waiting when not all wolves voted`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager())
        )
        game.handleNightAction(game.findPlayer("W1"), "KILL", "V1")

        assertEquals("Waiting for other werewolf...", game.getWolfStatus())
    }

    @Test
    fun `getWolfStatus returns disagreement message when wolves disagree`() {
        val game = createNightGame(
            listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager())
        )
        game.handleNightAction(game.findPlayer("W1"), "KILL", "V1")
        game.handleNightAction(game.findPlayer("W2"), "KILL", "Seer1")

        assertEquals("Wolves have selected different targets! You must agree.", game.getWolfStatus())
    }


    @Test
    fun `addPlayer adds single player`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        game.addPlayer(Player("P1"))

        assertEquals(1, game.players.size)
        assertEquals("P1", game.players[0].name)
    }

    @Test
    fun `addPlayers adds multiple players`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        game.addPlayers(listOf(Player("P1"), Player("P2")))

        assertEquals(2, game.players.size)
    }

    @Test
    fun `markPlayerReady adds to ready set`() {
        val game = Game("test-id", "Test Game", GameMode.ONLINE)
        game.markPlayerReady("P1")

        assertTrue(game.readyPlayers.contains("P1"))
    }
}
