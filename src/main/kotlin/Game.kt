package com.afonsobenedito

import com.afonsobenedito.werewolf.model.Player
import com.afonsobenedito.werewolf.model.roles.*

open class Game(
    val name: String
) {
    var status: GameStatus = GameStatus.NOT_STARTED
        protected set
    
    val players: MutableList<Player> = mutableListOf()
    var winner: Winner? = null
        private set

    fun addPlayers(newPlayers: List<Player>) {
        players.addAll(newPlayers)
    }

    open fun startGame() {
        assignRoles()
        status = GameStatus.IN_PROGRESS
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
}