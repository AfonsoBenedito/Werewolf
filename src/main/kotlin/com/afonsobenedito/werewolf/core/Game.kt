package com.afonsobenedito.werewolf.core

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.*

open class Game(
    val id: String,
    val name: String,
    val mode: GameMode
) {
    var status: GameStatus = GameStatus.NOT_STARTED
        protected set
    
    val players: MutableList<Player> = mutableListOf()
    var winner: Winner? = null
        private set

    var phase: GamePhase = GamePhase.NIGHT
    var currentTurn: String = ""
    var dayCount: Int = 0
    var pendingDeathId: String? = null
    var lastDeadPlayerName: String? = null
    val votes: MutableMap<String, String> = mutableMapOf()
    val readyPlayers: MutableSet<String> = mutableSetOf()

    val wolfVotes: MutableMap<String, String> = mutableMapOf()

    fun addPlayers(newPlayers: List<Player>) {
        players.addAll(newPlayers)
    }

    open fun startGame() {
        assignRoles()
        status = GameStatus.IN_PROGRESS
        phase = GamePhase.NIGHT
        dayCount = 1
        startNightPhase()
    }

    private fun assignRoles() {
        val shuffledPlayers = players.shuffled()
        val numPlayers = players.size
        val numWolves = if (numPlayers >= 6) 2 else 1

        var currentIndex = 0

        // Wolves
        for (i in 0 until numWolves) {
            shuffledPlayers[currentIndex++].assignRole(Wolf())
        }

        // Seer
        shuffledPlayers[currentIndex++].assignRole(Seer())

        // Medic
        shuffledPlayers[currentIndex++].assignRole(Medic())

        while (currentIndex < numPlayers) {
            shuffledPlayers[currentIndex++].assignRole(Villager())
        }
    }

    var seerHasPeeked = false

    fun startNightPhase() {
        wolfVotes.clear()
        seerHasPeeked = false
        val turnOrder = listOf("Wolf", "Seer", "Medic")
        
        for (roleName in turnOrder) {
            if (hasAliveRole(roleName)) {
                currentTurn = roleName
                return
            }
        }
        advancePhase()
    }
    fun handleNightAction(actor: Player, actionType: String, targetId: String?): String? {
        val target = players.find { it.name == targetId }
        
        // 1. Validate Turn
        val expectedRoleName = when(currentTurn) {
            "Wolf" -> "Wolf"
            "Seer" -> "Seer"
            "Medic" -> "Medic"
            else -> throw IllegalArgumentException("It is not your turn")
        }
        
        if (actor.role !is Wolf && actor.role?.name != expectedRoleName) {
             // Special case for Wolf: role name is "Werewolf" usually, but turn is "Wolf". 
             // Logic below handles specific role checks.
             // Simpler check:
        }
        
        // Better validation based on role instance
        val isCorrectRole = when (currentTurn) {
            "Wolf" -> actor.role is Wolf
            "Seer" -> actor.role is Seer
            "Medic" -> actor.role is Medic
            else -> false
        }
        
        if (!isCorrectRole) throw IllegalArgumentException("It is not your turn! Current turn: $currentTurn")

        // 2. Execute Action
        return when (actionType) {
            "KILL" -> {
                if (actor.role !is Wolf) throw IllegalArgumentException("Only Wolves can kill")
                if (target != null && target.role is Wolf) {
                     throw IllegalArgumentException("Wolves cannot kill other Wolves!")
                }
                
                // --- Consensus Logic ---
                if (target != null) {
                    wolfVotes[actor.name] = target.name
                }
                
                val aliveWolves = players.count { it.isAlive && it.role is Wolf }
                
                if (wolfVotes.size < aliveWolves) {
                    return "Waiting for other werewolf..."
                }
                
                // All wolves have voted, check consensus
                val uniqueTargets = wolfVotes.values.toSet()
                if (uniqueTargets.size == 1) {
                     // Consensus reached
                     val decision = uniqueTargets.first()
                     if (decision != "SKIP") {
                        pendingDeathId = decision
                     }
                     wolfVotes.clear()
                     advanceTurn()
                     null
                } else {
                     "Wolves have selected different targets! You must agree."
                }
            }
            "HEAL" -> {
                if (actor.role !is Medic) throw IllegalArgumentException("Only Medic can heal")
                if (target != null && pendingDeathId == target.name) {
                     pendingDeathId = null // Saved!
                }
                advanceTurn()
                null
            }
            "PEEK" -> {
                if (actor.role !is Seer) throw IllegalArgumentException("Only Seer can peek")
                if (seerHasPeeked) throw IllegalArgumentException("You have already peeked!")
                
                val targetRole = target?.role ?: return null
                seerHasPeeked = true
                // Do NOT advance turn immediately. User must click "Done" (SKIP)
                
                if (targetRole is Villager) "${target.name} is just a Regular Villager" else "${target.name} is not just a Regular Villager"
            }
            "SKIP" -> {
                if (currentTurn == "Wolf") {
                    wolfVotes[actor.name] = "SKIP"
                    
                    val aliveWolves = players.count { it.isAlive && it.role is Wolf }
                    if (wolfVotes.size < aliveWolves) return "Waiting for other werewolf..."
                    
                    val uniqueTargets = wolfVotes.values.toSet()
                    if (uniqueTargets.size == 1) {
                         // Agreed to skip
                         pendingDeathId = null
                         wolfVotes.clear()
                         advanceTurn()
                         null
                    } else {
                         "Wolves have selected different targets! You must agree."
                    }
                } else {
                    advanceTurn()
                    null
                }
            }
            else -> throw IllegalArgumentException("Invalid action for Night")
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
            // Removed "already voted" check
            // if (votes.containsKey(voter.name)) {
            //      throw IllegalArgumentException("You have already voted!")
            // }
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
        
        // Removed "already voted" check
        // if (votes.containsKey(voter.name)) {
        //      throw IllegalArgumentException("You have already voted!")
        // }
        
        votes[voter.name] = targetName
        
        checkVoteCompletion()
    }

    fun processOfflineAction(actionType: String, playerId: String, targetId: String?) {
         val target = players.find { it.name == targetId }
         
         when (actionType) {
             "KILL" -> {
                 if (target != null && target.role is Wolf) {
                     throw IllegalArgumentException("Wolves cannot kill other Wolves!")
                 }
                 if (target != null) pendingDeathId = target.name
                 advanceTurn()
             }
             "HEAL" -> { 
                 if (target != null && pendingDeathId == target.name) {
                     pendingDeathId = null // Saved!
                 }
                 advanceTurn()
             }
             "PEEK" -> { 
                 advanceTurn()
             }
             "NEXT_TURN" -> advanceTurn() 
             "NEXT_PHASE" -> advancePhase()
             "ELIMINATE" -> {
                 target?.die()
                 checkPhaseTransition()
                 if (phase != GamePhase.FINISHED) {
                    phase = GamePhase.NIGHT
                    dayCount++
                    currentTurn = "Wolf"
                    lastDeadPlayerName = null 
                 }
             }
             "VOTE" -> {
                 val voterId = playerId
                 if (voterId == "Master") return 
                 if (target == null) throw IllegalArgumentException("Vote target needed")
                 
                 // Removed "already voted" check
                 // if (votes.containsKey(voterId)) {
                 //     throw IllegalArgumentException("Player $voterId has already voted!")
                 // }
                 
                 votes[voterId] = target.name
                 
                 val alivePlayers = players.filter { it.isAlive }
                 
                 if (votes.size >= alivePlayers.size) {
                     tallyVotes()
                 }
             }
         }
         checkPhaseTransition()
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
        
        phase = GamePhase.DAY_RESULTS
        currentTurn = "" 
        
        checkPhaseTransition()
    }

    private fun advanceTurn() {
        if (phase != GamePhase.NIGHT) return
        
        val turnOrder = listOf("Wolf", "Seer", "Medic")
        val currentIndex = turnOrder.indexOf(currentTurn)
        
        // Find next valid turn
        for (i in currentIndex + 1 until turnOrder.size) {
            val nextTurn = turnOrder[i]
            if (hasAliveRole(nextTurn)) {
                currentTurn = nextTurn
                return
            }
        }
        
        // If no more turns in the list, end night
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
                wolfVotes.clear() // Ensure cleanup
            }
            GamePhase.DAY_DISCUSSION -> {
                phase = GamePhase.DAY_VOTING
            }
            GamePhase.DAY_VOTING -> {
                tallyVotes() 
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

    fun checkWinCondition(): Boolean {
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
        if (checkWinCondition()) {
            phase = GamePhase.FINISHED
        }
    }
}