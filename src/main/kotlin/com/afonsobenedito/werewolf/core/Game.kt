package com.afonsobenedito.werewolf.core

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*

open class Game(
    val id: String,
    val name: String,
    val mode: GameMode
) {
    companion object {
        private val TURN_ORDER = listOf("Wolf", "Seer", "Medic")
        const val WOLF_VOTE_NO_TARGET = "NO_TARGET"
        const val OFFLINE_MASTER = "Master"
    }

    var status: GameStatus = GameStatus.NOT_STARTED
        protected set

    val players: MutableList<Player> = mutableListOf()
    var winner: Winner? = null
        private set

    var phase: GamePhase = GamePhase.NIGHT
        internal set
    var currentTurn: String = ""
        private set
    var dayCount: Int = 0
        internal set
    var pendingDeathId: String? = null
        private set
    var lastDeadPlayerName: String? = null
        private set
    val votes: MutableMap<String, String> = mutableMapOf()
    val readyPlayers: MutableSet<String> = mutableSetOf()

    val wolfVotes: MutableMap<String, String> = mutableMapOf()
    var seerHasPeeked = false
        private set

    fun addPlayer(player: Player) {
        players.add(player)
    }

    fun addPlayers(newPlayers: List<Player>) {
        players.addAll(newPlayers)
    }

    fun markPlayerReady(playerName: String) {
        readyPlayers.add(playerName)
    }

    open fun startGame() {
        assignRoles()
        status = GameStatus.IN_PROGRESS
        phase = GamePhase.NIGHT
        dayCount = 1
        startNightPhase()
    }

    private fun assignRoles() {
        require(players.size >= 4) { "Need at least 4 players to start a game" }
        val shuffledPlayers = players.shuffled()
        val numPlayers = players.size
        val numWolves = if (numPlayers >= 6) 2 else 1

        var currentIndex = 0

        for (i in 0 until numWolves) {
            shuffledPlayers[currentIndex++].assignRole(Wolf())
        }

        shuffledPlayers[currentIndex++].assignRole(Seer())

        shuffledPlayers[currentIndex++].assignRole(Medic())

        while (currentIndex < numPlayers) {
            shuffledPlayers[currentIndex++].assignRole(Villager())
        }
    }

    fun startNightPhase() {
        wolfVotes.clear()
        seerHasPeeked = false

        for (roleName in TURN_ORDER) {
            if (hasAliveRole(roleName)) {
                currentTurn = roleName
                return
            }
        }
        advancePhase()
    }

    fun handleNightAction(actor: Player, actionType: String, targetId: String?): String? {
        val target = players.find { it.name == targetId }

        val isCorrectRole = when (currentTurn) {
            "Wolf" -> actor.role is Wolf
            "Seer" -> actor.role is Seer
            "Medic" -> actor.role is Medic
            else -> false
        }
        if (!isCorrectRole) throw IllegalArgumentException("It is not your turn! Current turn: $currentTurn")

        return when (actionType) {
            "KILL" -> {
                if (target != null && target.role is Wolf) {
                     throw IllegalArgumentException("Wolves cannot kill other Wolves!")
                }
                wolfVotes[actor.name] = target?.name ?: WOLF_VOTE_NO_TARGET
                resolveWolfConsensus()
            }
            "HEAL" -> {
                if (target != null && pendingDeathId == target.name) {
                     pendingDeathId = null
                }
                advanceTurn()
                null
            }
            "PEEK" -> {
                if (seerHasPeeked) throw IllegalArgumentException("You have already peeked!")

                val targetRole = target?.role ?: return null
                seerHasPeeked = true

                if (targetRole is Villager) "${target.name} is just a Regular Villager" else "${target.name} is not just a Regular Villager"
            }
            "SKIP" -> {
                if (currentTurn == "Wolf") {
                    wolfVotes[actor.name] = WOLF_VOTE_NO_TARGET
                    resolveWolfConsensus()
                } else {
                    advanceTurn()
                    null
                }
            }
            else -> throw IllegalArgumentException("Invalid action for Night")
        }
    }

    private fun resolveWolfConsensus(): String? {
        val aliveWolves = players.count { it.isAlive && it.role is Wolf }
        if (wolfVotes.size < aliveWolves) return "Waiting for other werewolf..."

        val uniqueTargets = wolfVotes.values.toSet()
        return if (uniqueTargets.size == 1) {
            val decision = uniqueTargets.first()
            if (decision != WOLF_VOTE_NO_TARGET) {
                pendingDeathId = decision
            }
            wolfVotes.clear()
            advanceTurn()
            null
        } else {
            "Wolves have selected different targets! You must agree."
        }
    }

    fun getWolfStatus(): String? {
        if (phase != GamePhase.NIGHT || currentTurn != "Wolf") return null

        val aliveWolves = players.count { it.isAlive && it.role is Wolf }
        if (wolfVotes.size < aliveWolves) return "Waiting for other werewolf..."

        val uniqueTargets = wolfVotes.values.toSet()
        if (uniqueTargets.size > 1) return "Wolves have selected different targets! You must agree."

        return null
    }

    fun handleVotingAction(voter: Player, actionType: String, targetId: String?) {
        if (actionType == "SKIP") {
            votes[voter.name] = "ABSTAIN"
            checkVoteCompletion()
            return
        }

        if (actionType == "UNVOTE") {
            votes.remove(voter.name)
            return
        }

        if (actionType != "VOTE") throw IllegalArgumentException("Only VOTE action allowed in voting phase")

        val targetName = targetId ?: throw IllegalArgumentException("Vote target needed")
        val target = players.find { it.name == targetName } ?: throw IllegalArgumentException("Target not found")
        if (!target.isAlive) throw IllegalArgumentException("Cannot vote for dead player")

        votes[voter.name] = targetName

        checkVoteCompletion()
    }

    fun processOfflineAction(actionType: String, playerId: String, targetId: String?): String? {
         var result: String? = null
         when (actionType) {
             "KILL" -> {
                 val wolves = players.filter { it.isAlive && it.role is Wolf }
                 for (wolf in wolves) {
                     handleNightAction(wolf, "KILL", targetId)
                 }
             }
             "HEAL" -> {
                 val medic = players.find { it.isAlive && it.role is Medic }
                     ?: throw IllegalArgumentException("No alive Medic found")
                 handleNightAction(medic, "HEAL", targetId)
             }
             "PEEK" -> {
                 val seer = players.find { it.isAlive && it.role is Seer }
                     ?: throw IllegalArgumentException("No alive Seer found")
                 val rawResult = handleNightAction(seer, "PEEK", targetId)
                 result = if (rawResult?.contains("not just") == true) "NOT a Villager" else "Villager"
             }
             "NEXT_TURN" -> advanceTurn()
             "NEXT_PHASE" -> advancePhase()
             "ELIMINATE" -> {
                 val target = players.find { it.name == targetId }
                 target?.die()
                 checkPhaseTransition()
                 if (phase != GamePhase.FINISHED) {
                    phase = GamePhase.NIGHT
                    dayCount++
                    lastDeadPlayerName = null
                    startNightPhase()
                 }
             }
             "VOTE" -> {
                 if (playerId == OFFLINE_MASTER) return null
                 val voter = players.find { it.name == playerId }
                     ?: throw IllegalArgumentException("Voter not found")
                 if (targetId == "SKIP") {
                     handleVotingAction(voter, "SKIP", null)
                 } else {
                     handleVotingAction(voter, "VOTE", targetId)
                 }
             }
         }
         checkPhaseTransition()
         return result
    }

    private fun checkVoteCompletion() {
        val alivePlayersCount = players.count { it.isAlive }
        if (votes.size >= alivePlayersCount) {
             tallyVotes()
        }
    }

    private fun tallyVotes() {
        val voteCounts = votes.values.groupingBy { it }.eachCount()
        val maxVotes = voteCounts.maxByOrNull { it.value }?.value ?: 0
        val candidates = voteCounts.filter { it.value == maxVotes }.keys

        if (candidates.size == 1) {
            val victimName = candidates.first()
            if (victimName == "ABSTAIN") {
                lastDeadPlayerName = null
            } else {
                val victim = players.find { it.name == victimName }
                victim?.die()
                lastDeadPlayerName = victimName
            }
        } else {
            lastDeadPlayerName = null
        }

        votes.clear()
        readyPlayers.clear()

        phase = GamePhase.DAY_RESULTS
        currentTurn = ""

        checkPhaseTransition()
    }

    private fun advanceTurn() {
        if (phase != GamePhase.NIGHT) return

        val currentIndex = TURN_ORDER.indexOf(currentTurn)

        for (i in currentIndex + 1 until TURN_ORDER.size) {
            val nextTurn = TURN_ORDER[i]
            if (hasAliveRole(nextTurn)) {
                currentTurn = nextTurn
                return
            }
        }

        advancePhase()
    }

    fun advancePhase() {
        when (phase) {
            GamePhase.NIGHT -> {
                if (pendingDeathId != null) {
                    val victim = players.find { it.name == pendingDeathId }
                    victim?.die()
                    lastDeadPlayerName = pendingDeathId
                    pendingDeathId = null
                } else {
                    lastDeadPlayerName = null
                }

                phase = GamePhase.DAY_DISCUSSION
                currentTurn = ""
                readyPlayers.clear()
                wolfVotes.clear()
            }
            GamePhase.DAY_DISCUSSION -> {
                lastDeadPlayerName = null
                phase = GamePhase.DAY_VOTING
            }
            GamePhase.DAY_VOTING -> {
                tallyVotes()
                readyPlayers.clear()
            }
            GamePhase.DAY_RESULTS -> {
                phase = GamePhase.NIGHT
                dayCount++
                lastDeadPlayerName = null
                startNightPhase()
            }
            GamePhase.FINISHED -> {}
        }
        checkPhaseTransition()
    }

    private fun hasAliveRole(roleName: String): Boolean {
        return players.any { player ->
            player.isAlive && when (roleName) {
                "Wolf" -> player.role is Wolf
                "Seer" -> player.role is Seer
                "Medic" -> player.role is Medic
                else -> false
            }
        }
    }

    fun evaluateWinCondition(): Boolean {
        val aliveWolves = players.count { it.isAlive && it.role is Wolf }
        val aliveVillagers = players.count { it.isAlive && it.role !is Wolf }

        if (aliveWolves == 0) {
            status = GameStatus.FINISHED
            winner = Winner.VILLAGERS
            return true
        }

        if (aliveWolves >= aliveVillagers) {
            status = GameStatus.FINISHED
            winner = Winner.WEREWOLVES
            return true
        }
        return false
    }

    fun checkPhaseTransition() {
        if (evaluateWinCondition()) {
            phase = GamePhase.FINISHED
        }
    }
}
