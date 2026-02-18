package com.afonsobenedito.werewolf.web.mapper

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import com.afonsobenedito.werewolf.core.GamePhase
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.core.Winner
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameMapperTest {

    private val mapper = GameMapper()

    // --- Helpers ---

    /** Game subclass that allows setting status from tests. */
    private class TestGame(id: String, name: String, mode: GameMode) : Game(id, name, mode) {
        fun forceStatus(s: GameStatus) { status = s }
    }

    private fun gameWithRoles(
        mode: GameMode = GameMode.ONLINE,
        playerRoles: List<Pair<String, Role>> = listOf(
            "Wolf1" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "Villager1" to Villager()
        ),
        status: GameStatus = GameStatus.NOT_STARTED
    ): TestGame {
        val game = TestGame("id", "name", mode)
        playerRoles.forEach { (name, role) ->
            val player = Player(name)
            player.assignRole(role)
            game.addPlayer(player)
        }
        if (status != GameStatus.NOT_STARTED) game.forceStatus(status)
        return game
    }

    private fun nightGame(
        mode: GameMode = GameMode.ONLINE,
        currentTurn: String = "Wolf"
    ): TestGame {
        val game = gameWithRoles(mode = mode, status = GameStatus.IN_PROGRESS)
        game.phase = GamePhase.NIGHT
        game.dayCount = 1
        game.startNightPhase()
        // Advance to the desired turn by skipping
        while (game.currentTurn != currentTurn && game.phase == GamePhase.NIGHT) {
            val actor = game.players.first { p ->
                p.isAlive && when (game.currentTurn) {
                    "Wolf" -> p.role is Wolf
                    "Seer" -> p.role is Seer
                    "Medic" -> p.role is Medic
                    else -> false
                }
            }
            game.handleNightAction(actor, "SKIP", null)
        }
        return game
    }

    private fun inProgressGame(
        mode: GameMode = GameMode.ONLINE,
        phase: GamePhase = GamePhase.DAY_DISCUSSION
    ): TestGame {
        val game = gameWithRoles(mode = mode, status = GameStatus.IN_PROGRESS)
        game.phase = phase
        return game
    }

    // ===================
    // Legacy phase format
    // ===================

    @Test
    fun `phase format during night includes current turn`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("NIGHT - Wolf", response.phase)
    }

    @Test
    fun `phase format during day uses enum name`() {
        val game = inProgressGame(phase = GamePhase.DAY_DISCUSSION)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("DAY_DISCUSSION", response.phase)
    }

    @Test
    fun `phaseKey always uses enum name`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("NIGHT", response.phaseKey)
    }

    // ===================
    // currentTurn
    // ===================

    @Test
    fun `currentTurn set during night`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Wolf", response.currentTurn)
    }

    @Test
    fun `currentTurn null during day`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertNull(response.currentTurn)
    }

    // ===================
    // Vote masking
    // ===================

    @Test
    fun `votes masked during voting phase`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        game.votes["Wolf1"] = "Seer1"
        game.votes["Seer1"] = "Wolf1"

        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Seer1", response.votes["Wolf1"])
        assertEquals("SECRET", response.votes["Seer1"])
    }

    @Test
    fun `votes not masked outside voting phase`() {
        val game = inProgressGame(phase = GamePhase.DAY_RESULTS)
        game.votes["Wolf1"] = "Seer1"

        val response = mapper.toGameResponse(game, "Seer1")
        assertEquals("Seer1", response.votes["Wolf1"])
    }

    // ===================
    // Role visibility (shouldRevealRole)
    // ===================

    @Test
    fun `reveals own role`() {
        val game = gameWithRoles()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Werewolf", response.players.find { it.name == "Wolf1" }?.role)
    }

    @Test
    fun `hides others role in online mode`() {
        val game = gameWithRoles()
        val response = mapper.toGameResponse(game, "Seer1")
        assertEquals("Unknown", response.players.find { it.name == "Wolf1" }?.role)
    }

    @Test
    fun `wolf sees other wolf role`() {
        val game = gameWithRoles(
            playerRoles = listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic())
        )
        val response = mapper.toGameResponse(game, "W1")
        assertEquals("Werewolf", response.players.find { it.name == "W2" }?.role)
    }

    @Test
    fun `reveals dead player role`() {
        val game = gameWithRoles()
        game.players.find { it.name == "Wolf1" }!!.die()
        val response = mapper.toGameResponse(game, "Seer1")
        assertEquals("Werewolf", response.players.find { it.name == "Wolf1" }?.role)
    }

    @Test
    fun `reveals all roles in offline mode`() {
        val game = gameWithRoles(mode = GameMode.OFFLINE)
        val response = mapper.toGameResponse(game, null)
        assertTrue(response.players.none { it.role == "Unknown" })
    }

    @Test
    fun `reveals all roles when game finished`() {
        val game = gameWithRoles()
        game.players.find { it.name == "Wolf1" }!!.die()
        game.evaluateWinCondition()
        val response = mapper.toGameResponse(game, "Seer1")
        assertTrue(response.players.all { it.role != "Unknown" })
    }

    // ===================
    // isMyTurn
    // ===================

    @Test
    fun `isMyTurn true for wolf during wolf turn`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.isMyTurn)
    }

    @Test
    fun `isMyTurn false for seer during wolf turn`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Seer1")
        assertFalse(response.isMyTurn)
    }

    @Test
    fun `isMyTurn true for seer during seer turn`() {
        val game = nightGame(currentTurn = "Seer")
        val response = mapper.toGameResponse(game, "Seer1")
        assertTrue(response.isMyTurn)
    }

    @Test
    fun `isMyTurn true for medic during medic turn`() {
        val game = nightGame(currentTurn = "Medic")
        val response = mapper.toGameResponse(game, "Medic1")
        assertTrue(response.isMyTurn)
    }

    @Test
    fun `isMyTurn true during day discussion when in progress`() {
        val game = inProgressGame(phase = GamePhase.DAY_DISCUSSION)
        val response = mapper.toGameResponse(game, "Villager1")
        assertTrue(response.isMyTurn)
    }

    @Test
    fun `isMyTurn true during day voting when in progress`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.isMyTurn)
    }

    @Test
    fun `isMyTurn false for dead player`() {
        val game = nightGame()
        game.players.find { it.name == "Wolf1" }!!.die()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.isMyTurn)
    }

    @Test
    fun `isMyTurn false when game not in progress`() {
        val game = gameWithRoles()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.isMyTurn)
    }

    @Test
    fun `isMyTurn false during finished phase`() {
        val game = nightGame()
        game.phase = GamePhase.FINISHED
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.isMyTurn)
    }

    @Test
    fun `isMyTurn false when no requester during night`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, null)
        assertFalse(response.isMyTurn)
    }

    // ===================
    // availableActions
    // ===================

    @Test
    fun `available actions KILL during wolf turn`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals(listOf("KILL"), response.availableActions)
    }

    @Test
    fun `available actions PEEK during seer turn`() {
        val game = nightGame(currentTurn = "Seer")
        val response = mapper.toGameResponse(game, "Seer1")
        assertEquals(listOf("PEEK"), response.availableActions)
    }

    @Test
    fun `available actions HEAL during medic turn`() {
        val game = nightGame(currentTurn = "Medic")
        val response = mapper.toGameResponse(game, "Medic1")
        assertEquals(listOf("HEAL"), response.availableActions)
    }

    @Test
    fun `available actions READY_TO_VOTE during day discussion`() {
        val game = inProgressGame(phase = GamePhase.DAY_DISCUSSION)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals(listOf("READY_TO_VOTE"), response.availableActions)
    }

    @Test
    fun `available actions VOTE and ABSTAIN during day voting`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals(listOf("VOTE", "ABSTAIN"), response.availableActions)
    }

    @Test
    fun `available actions empty when not in progress`() {
        val game = gameWithRoles()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.availableActions.isEmpty())
    }

    @Test
    fun `available actions empty for dead player`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        game.players.find { it.name == "Wolf1" }!!.die()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.availableActions.isEmpty())
    }

    // ===================
    // canSkip
    // ===================

    @Test
    fun `canSkip false for wolf`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.canSkip)
    }

    @Test
    fun `canSkip true for seer during seer turn`() {
        val game = nightGame(currentTurn = "Seer")
        val response = mapper.toGameResponse(game, "Seer1")
        assertTrue(response.canSkip)
    }

    @Test
    fun `canSkip true for medic during medic turn`() {
        val game = nightGame(currentTurn = "Medic")
        val response = mapper.toGameResponse(game, "Medic1")
        assertTrue(response.canSkip)
    }

    @Test
    fun `canSkip false during day`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Seer1")
        assertFalse(response.canSkip)
    }

    @Test
    fun `canSkip offline seer turn without requester`() {
        val game = nightGame(mode = GameMode.OFFLINE, currentTurn = "Seer")
        val response = mapper.toGameResponse(game, null)
        assertTrue(response.canSkip)
    }

    @Test
    fun `canSkip offline wolf turn without requester`() {
        val game = nightGame(mode = GameMode.OFFLINE)
        val response = mapper.toGameResponse(game, null)
        assertFalse(response.canSkip)
    }

    // ===================
    // isTargetable
    // ===================

    @Test
    fun `wolf cannot target other wolf at night`() {
        val game = gameWithRoles(
            playerRoles = listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic()),
            status = GameStatus.IN_PROGRESS
        )
        game.phase = GamePhase.NIGHT
        game.dayCount = 1
        game.startNightPhase()
        val response = mapper.toGameResponse(game, "W1")
        assertFalse(response.players.find { it.name == "W2" }!!.isTargetable)
    }

    @Test
    fun `wolf can target non-wolf at night`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.players.find { it.name == "Seer1" }!!.isTargetable)
    }

    @Test
    fun `seer cannot target self online`() {
        val game = nightGame(currentTurn = "Seer")
        val response = mapper.toGameResponse(game, "Seer1")
        assertFalse(response.players.find { it.name == "Seer1" }!!.isTargetable)
    }

    @Test
    fun `medic can target self`() {
        val game = nightGame(currentTurn = "Medic")
        val response = mapper.toGameResponse(game, "Medic1")
        assertTrue(response.players.find { it.name == "Medic1" }!!.isTargetable)
    }

    @Test
    fun `cannot target dead player`() {
        val game = nightGame()
        game.players.find { it.name == "Villager1" }!!.die()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.players.find { it.name == "Villager1" }!!.isTargetable)
    }

    @Test
    fun `cannot vote for self during voting`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.players.find { it.name == "Wolf1" }!!.isTargetable)
        assertTrue(response.players.find { it.name == "Seer1" }!!.isTargetable)
    }

    @Test
    fun `offline voting all alive are targetable`() {
        val game = inProgressGame(mode = GameMode.OFFLINE, phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, null)
        val aliveTargetable = response.players.filter { it.isAlive && it.isTargetable }
        assertEquals(response.players.count { it.isAlive }, aliveTargetable.size)
    }

    @Test
    fun `not targetable when game not in progress`() {
        val game = gameWithRoles()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.players.none { it.isTargetable })
    }

    @Test
    fun `offline seer cannot target self`() {
        val game = nightGame(mode = GameMode.OFFLINE, currentTurn = "Seer")
        val response = mapper.toGameResponse(game, null)
        assertFalse(response.players.find { it.name == "Seer1" }!!.isTargetable)
    }

    // ===================
    // hasVoted
    // ===================

    @Test
    fun `hasVoted true when player has voted during voting`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        game.votes["Wolf1"] = "Seer1"
        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.players.find { it.name == "Wolf1" }!!.hasVoted)
    }

    @Test
    fun `hasVoted false when player has not voted`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.players.find { it.name == "Wolf1" }!!.hasVoted)
    }

    @Test
    fun `hasVoted false outside voting phase`() {
        val game = inProgressGame(phase = GamePhase.DAY_DISCUSSION)
        game.votes["Wolf1"] = "Seer1"
        val response = mapper.toGameResponse(game, "Wolf1")
        assertFalse(response.players.find { it.name == "Wolf1" }!!.hasVoted)
    }

    // ===================
    // isOnWinningTeam
    // ===================

    @Test
    fun `isOnWinningTeam null when game not finished`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertNull(response.players.find { it.name == "Wolf1" }!!.isOnWinningTeam)
    }

    @Test
    fun `isOnWinningTeam true for wolf when wolves win`() {
        val game = gameWithRoles()
        game.players.find { it.name == "Seer1" }!!.die()
        game.players.find { it.name == "Medic1" }!!.die()
        game.evaluateWinCondition()
        assertEquals(Winner.WEREWOLVES, game.winner)

        val response = mapper.toGameResponse(game, "Wolf1")
        assertTrue(response.players.find { it.name == "Wolf1" }!!.isOnWinningTeam!!)
        assertFalse(response.players.find { it.name == "Villager1" }!!.isOnWinningTeam!!)
    }

    @Test
    fun `isOnWinningTeam true for villager when villagers win`() {
        val game = gameWithRoles()
        game.players.find { it.name == "Wolf1" }!!.die()
        game.evaluateWinCondition()
        assertEquals(Winner.VILLAGERS, game.winner)

        val response = mapper.toGameResponse(game, "Seer1")
        assertTrue(response.players.find { it.name == "Seer1" }!!.isOnWinningTeam!!)
        assertFalse(response.players.find { it.name == "Wolf1" }!!.isOnWinningTeam!!)
    }

    // ===================
    // phaseDisplayName
    // ===================

    @Test
    fun `phaseDisplayName for wolf turn`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Werewolves' Turn", response.phaseDisplayName)
    }

    @Test
    fun `phaseDisplayName for seer turn`() {
        val game = nightGame(currentTurn = "Seer")
        val response = mapper.toGameResponse(game, "Seer1")
        assertEquals("Seer's Turn", response.phaseDisplayName)
    }

    @Test
    fun `phaseDisplayName for medic turn`() {
        val game = nightGame(currentTurn = "Medic")
        val response = mapper.toGameResponse(game, "Medic1")
        assertEquals("Medic's Turn", response.phaseDisplayName)
    }

    @Test
    fun `phaseDisplayName for day discussion`() {
        val game = inProgressGame(phase = GamePhase.DAY_DISCUSSION)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Village Discussion", response.phaseDisplayName)
    }

    @Test
    fun `phaseDisplayName for day voting`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Voting Phase", response.phaseDisplayName)
    }

    @Test
    fun `phaseDisplayName for day results`() {
        val game = inProgressGame(phase = GamePhase.DAY_RESULTS)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Voting Results", response.phaseDisplayName)
    }

    @Test
    fun `phaseDisplayName for finished`() {
        val game = inProgressGame(phase = GamePhase.FINISHED)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Game Over", response.phaseDisplayName)
    }

    // ===================
    // turnInstruction
    // ===================

    @Test
    fun `turnInstruction for wolf turn`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Choose a victim to kill.", response.turnInstruction)
    }

    @Test
    fun `turnInstruction for seer turn`() {
        val game = nightGame(currentTurn = "Seer")
        val response = mapper.toGameResponse(game, "Seer1")
        assertEquals("Choose a player to inspect.", response.turnInstruction)
    }

    @Test
    fun `turnInstruction for medic turn`() {
        val game = nightGame(currentTurn = "Medic")
        val response = mapper.toGameResponse(game, "Medic1")
        assertEquals("Choose a player to save.", response.turnInstruction)
    }

    @Test
    fun `turnInstruction for day discussion`() {
        val game = inProgressGame(phase = GamePhase.DAY_DISCUSSION)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Discuss who might be a wolf.", response.turnInstruction)
    }

    @Test
    fun `turnInstruction for day voting`() {
        val game = inProgressGame(phase = GamePhase.DAY_VOTING)
        val response = mapper.toGameResponse(game, "Wolf1")
        assertEquals("Vote to eliminate a suspect.", response.turnInstruction)
    }

    @Test
    fun `turnInstruction null when game not in progress`() {
        val game = gameWithRoles()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertNull(response.turnInstruction)
    }

    // ===================
    // nightStatus
    // ===================

    @Test
    fun `nightStatus null for non-wolf player`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, "Seer1")
        assertNull(response.nightStatus)
    }

    @Test
    fun `nightStatus null when not wolf turn`() {
        val game = nightGame(currentTurn = "Seer")
        val response = mapper.toGameResponse(game, "Wolf1")
        assertNull(response.nightStatus)
    }

    @Test
    fun `nightStatus null when no playerId`() {
        val game = nightGame()
        val response = mapper.toGameResponse(game, null)
        assertNull(response.nightStatus)
    }

    @Test
    fun `nightStatus shows disagreement to wolf who voted`() {
        val game = gameWithRoles(
            playerRoles = listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager()),
            status = GameStatus.IN_PROGRESS
        )
        game.phase = GamePhase.NIGHT
        game.dayCount = 1
        game.startNightPhase()
        game.handleNightAction(game.players.find { it.name == "W1" }!!, "KILL", "V1")
        game.handleNightAction(game.players.find { it.name == "W2" }!!, "KILL", "Seer1")

        val response = mapper.toGameResponse(game, "W1")
        assertEquals("Wolves have selected different targets! You must agree.", response.nightStatus)
    }

    @Test
    fun `nightStatus null for wolf who has not yet voted while waiting`() {
        val game = gameWithRoles(
            playerRoles = listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager()),
            status = GameStatus.IN_PROGRESS
        )
        game.phase = GamePhase.NIGHT
        game.dayCount = 1
        game.startNightPhase()
        game.handleNightAction(game.players.find { it.name == "W1" }!!, "KILL", "V1")

        val response = mapper.toGameResponse(game, "W2")
        assertNull(response.nightStatus)
    }

    @Test
    fun `nightStatus shows waiting for wolf who already voted`() {
        val game = gameWithRoles(
            playerRoles = listOf("W1" to Wolf(), "W2" to Wolf(), "Seer1" to Seer(), "Medic1" to Medic(), "V1" to Villager()),
            status = GameStatus.IN_PROGRESS
        )
        game.phase = GamePhase.NIGHT
        game.dayCount = 1
        game.startNightPhase()
        game.handleNightAction(game.players.find { it.name == "W1" }!!, "KILL", "V1")

        val response = mapper.toGameResponse(game, "W1")
        assertEquals("Waiting for other werewolf...", response.nightStatus)
    }

    // ===================
    // canStart
    // ===================

    @Test
    fun `canStart true for host with enough players`() {
        val game = Game("id", "name", GameMode.ONLINE)
        (1..4).forEach { game.addPlayer(Player("P$it")) }

        val response = mapper.toGameResponse(game, "P1")
        assertTrue(response.canStart)
    }

    @Test
    fun `canStart false for non-host`() {
        val game = Game("id", "name", GameMode.ONLINE)
        (1..4).forEach { game.addPlayer(Player("P$it")) }

        val response = mapper.toGameResponse(game, "P2")
        assertFalse(response.canStart)
    }

    @Test
    fun `canStart false with not enough players`() {
        val game = Game("id", "name", GameMode.ONLINE)
        game.addPlayer(Player("P1"))
        game.addPlayer(Player("P2"))

        val response = mapper.toGameResponse(game, "P1")
        assertFalse(response.canStart)
    }

    @Test
    fun `canStart false when game already started`() {
        val game = gameWithRoles(status = GameStatus.IN_PROGRESS)
        val response = mapper.toGameResponse(game, game.players.first().name)
        assertFalse(response.canStart)
    }

    // ===================
    // isHost
    // ===================

    @Test
    fun `isHost true for first player`() {
        val game = Game("id", "name", GameMode.ONLINE)
        game.addPlayer(Player("Host"))
        game.addPlayer(Player("P2"))

        val response = mapper.toGameResponse(game, "Host")
        assertTrue(response.isHost)
    }

    @Test
    fun `isHost false for non-first player`() {
        val game = Game("id", "name", GameMode.ONLINE)
        game.addPlayer(Player("Host"))
        game.addPlayer(Player("P2"))

        val response = mapper.toGameResponse(game, "P2")
        assertFalse(response.isHost)
    }

    // ===================
    // Basic response fields
    // ===================

    @Test
    fun `response includes basic game info`() {
        val game = Game("game-123", "TestGame", GameMode.ONLINE)
        game.addPlayer(Player("P1"))

        val response = mapper.toGameResponse(game, "P1")

        assertEquals("game-123", response.id)
        assertEquals("NOT_STARTED", response.status)
        assertEquals(4, response.minPlayers)
        assertEquals(1, response.totalAliveCount)
        assertEquals(0, response.readyPlayerCount)
    }

    @Test
    fun `response includes winner when game finished`() {
        val game = gameWithRoles()
        game.players.find { it.name == "Wolf1" }!!.die()
        game.evaluateWinCondition()

        val response = mapper.toGameResponse(game, "Seer1")
        assertEquals("VILLAGERS", response.winner)
    }

    @Test
    fun `response winner null when game not finished`() {
        val game = gameWithRoles()
        val response = mapper.toGameResponse(game, "Wolf1")
        assertNull(response.winner)
    }
}
