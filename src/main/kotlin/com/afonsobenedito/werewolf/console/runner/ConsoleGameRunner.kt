package com.afonsobenedito.werewolf.console.runner

import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameStatus
import com.afonsobenedito.werewolf.console.controller.*
import com.afonsobenedito.werewolf.console.interaction.GameInteraction
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Medic
import com.afonsobenedito.werewolf.core.model.roles.Seer
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.core.Winner

class ConsoleGameRunner(
    private val game: Game,
    private val interaction: GameInteraction
) {

    fun run() {
        interaction.announce("Welcome to A Aldeia Adormece!")
        val players = interaction.setupPlayers()
        game.addPlayers(players)
        game.startGame()
        gameLoop()
    }

    private fun gameLoop() {
        var dayCount = 1
        while (game.status == GameStatus.IN_PROGRESS) {
            interaction.announce("\n--- Night $dayCount ---")
            nightPhase()
            
            if (checkWinCondition()) break

            interaction.announce("\n--- Day $dayCount ---")
            dayPhase()
            
            if (checkWinCondition()) break
            
            dayCount++
        }
    }

    private fun nightPhase() {
        announceNightStart()
        
        val players = game.players
        val aliveBeforeNight = players.filter { it.isAlive }.toSet()

        val wolfTarget = playWolfTurn(players)
        val medicTarget = playMedicTurn(players)
        playSeerTurn(players)
        
        // Resolve Night Actions
        if (wolfTarget != null) {
            if (wolfTarget != medicTarget) {
                wolfTarget.die()
            } else {
                // Saved by Medic!
            }
        }
        
        announceNightEnd()
        announceMorningReport(aliveBeforeNight)
    }

    private fun announceNightStart() {
        interaction.announce("The village goes to sleep. Close your eyes! (Functionally, press Enter to continue to secret turns...)")
        interaction.promptEnter()
    }

    private fun playWolfTurn(players: List<Player>): Player? {
        val activeWolves = players.filter { it.isAlive && it.role is Wolf }
        val wolfHandler = WolfTurnHandler()
        return wolfHandler.handleTurn(activeWolves, players, interaction)
    }

    private fun playMedicTurn(players: List<Player>): Player? {
        val activeMedics = players.filter { it.isAlive && it.role is Medic }
        val medicHandler = MedicTurnHandler()
        return medicHandler.handleTurn(activeMedics, players, interaction)
    }

    private fun playSeerTurn(players: List<Player>) {
        val activeSeers = players.filter { it.isAlive && it.role is Seer }
        val seerHandler = SeerTurnHandler()
        seerHandler.handleTurn(activeSeers, players, interaction)
    }

    private fun announceNightEnd() {
        interaction.clearScreen()
        interaction.announce("Everyone wake up!")
        interaction.promptEnter()
    }

    private fun announceMorningReport(aliveBeforeNight: Set<Player>) {
        interaction.clearScreen()
        interaction.announce("--- Morning Report ---")
        
        val aliveAfterNight = game.players.filter { it.isAlive }.toSet()
        val diedDuringNight = aliveBeforeNight - aliveAfterNight
        
        if (diedDuringNight.isNotEmpty()) {
            diedDuringNight.forEach { victim ->
                interaction.announce("${victim.name} was killed in the night!")
                interaction.announce("Their role was: ${victim.role?.name}")
            }
        } else {
            interaction.announce("No one died last night.")
        }
        interaction.promptEnter()
    }

    private fun dayPhase() {
        announceDiscussion()
        
        interaction.announce("Voting Phase.")
        val votes = collectVotes()
        announceVotingResults(votes)
        processElimination(votes)
        
        interaction.promptEnter()
    }

    private fun announceDiscussion() {
        interaction.announce("Discussion Phase. Discuss among yourselves who might be the Werewolf.")
        interaction.promptEnter()
    }

    private fun collectVotes(): Map<Player, Int> {
        val votes = mutableMapOf<Player, Int>()
        val players = game.players
        val activePlayers = players.filter { it.isAlive }
        
        for (voter in activePlayers) {
            interaction.clearScreen()
            val selectedPlayer = interaction.getPlayerSelection(
                activePlayers.filter { it != voter }, 
                "${voter.name}, cast your vote to eliminate:", 
                allowSkip = true
            )
            if (selectedPlayer != null) {
                votes[selectedPlayer] = votes.getOrDefault(selectedPlayer, 0) + 1
            }
            interaction.promptEnter()
        }
        return votes
    }

    private fun announceVotingResults(votes: Map<Player, Int>) {
        interaction.clearScreen()
        interaction.announce("Voting Results:")
        votes.forEach { (player, count) -> interaction.announce("${player.name}: $count votes") }
    }

    private fun processElimination(votes: Map<Player, Int>) {
        val maxVotes = votes.maxByOrNull { it.value }?.value ?: 0
        if (maxVotes > 0) {
            val potentialVictims = votes.filter { it.value == maxVotes }.keys.toList()
            if (potentialVictims.size == 1) {
                val victim = potentialVictims.first()
                interaction.announce("${victim.name} has been eliminated by the village!")
                victim.die()
                interaction.announce("Their role was: ${victim.role?.name}")
            } else {
                interaction.announce("Tie vote between ${potentialVictims.joinToString { it.name }}. No one is eliminated.")
            }
        } else {
            interaction.announce("No votes cast. No one is eliminated.")
        }
    }

    private fun checkWinCondition(): Boolean {
        if (game.checkWinCondition()) {
            if (game.winner == Winner.VILLAGERS) {
                interaction.announce("\n*** VILLAGERS WIN! All werewolves are eliminated. ***")
            } else if (game.winner == Winner.WEREWOLVES) {
                 interaction.announce("\n*** WEREWOLVES WIN! They outnumber the villagers. ***")
            }
            return true
        }
        return false
    }
}
