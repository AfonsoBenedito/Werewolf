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
    private val wolfHandler = WolfTurnHandler()
    private val seerHandler = SeerTurnHandler()
    private val medicHandler = MedicTurnHandler()

    fun run() {
        interaction.announce("Welcome to A Aldeia Adormece!")
        val players = interaction.setupPlayers()
        game.addPlayers(players)
        game.startGame()
        gameLoop()
    }

    private fun gameLoop() {
        while (game.status == GameStatus.IN_PROGRESS) {
            interaction.announce("\n--- Night ${game.dayCount} ---")
            nightPhase()

            if (checkWinCondition()) break

            interaction.announce("\n--- Day ${game.dayCount} ---")
            dayPhase()

            if (checkWinCondition()) break

            game.dayCount++
        }
    }

    private fun nightPhase() {
        announceNightStart()
        
        val players = game.players
        val aliveBeforeNight = players.filter { it.isAlive }.toSet()

        val wolfTarget = playWolfTurn(players)
        playSeerTurn(players)
        val medicTarget = playMedicTurn(players)

        if (wolfTarget != null) {
            if (wolfTarget != medicTarget) {
                wolfTarget.die()
            } else {
            }
        }
        
        announceNightEnd()
        announceMorningReport(aliveBeforeNight)
    }

    private fun announceNightStart() {
        interaction.announce("The Village goes to sleep...")
        interaction.promptEnter()
    }

    private fun playWolfTurn(players: List<Player>): Player? {
        val activeWolves = players.filter { it.isAlive && it.role is Wolf }
        return wolfHandler.handleTurn(activeWolves, players, interaction)
    }

    private fun playMedicTurn(players: List<Player>): Player? {
        val activeMedics = players.filter { it.isAlive && it.role is Medic }
        return medicHandler.handleTurn(activeMedics, players, interaction)
    }

    private fun playSeerTurn(players: List<Player>) {
        val activeSeers = players.filter { it.isAlive && it.role is Seer }
        seerHandler.handleTurn(activeSeers, players, interaction)
    }

    private fun announceNightEnd() {
        interaction.clearScreen()
        interaction.announce("The Village wakes up with the news that...")
        interaction.promptEnter()
    }

    private fun announceMorningReport(aliveBeforeNight: Set<Player>) {
        interaction.clearScreen()
        interaction.announce("--- Morning Report ---")
        
        val aliveAfterNight = game.players.filter { it.isAlive }.toSet()
        val diedDuringNight = aliveBeforeNight - aliveAfterNight
        
        if (diedDuringNight.isNotEmpty()) {
            diedDuringNight.forEach { victim ->
                interaction.announce("${victim.name} died last night!")
                interaction.announce("Their role was: ${victim.role?.name}")
            }
        } else {
            interaction.announce("It was a peaceful night.")
        }
        interaction.promptEnter()
    }

    private fun dayPhase() {
        announceDiscussion()
        
        interaction.announce("Village, Let's vote!")
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
                interaction.announce("${victim.name} was eliminated!")
                victim.die()
                interaction.announce("Their role was: ${victim.role?.name}")
            } else {
                interaction.announce("Tie vote between ${potentialVictims.joinToString { it.name }}. No one is eliminated.")
            }
        } else {
            interaction.announce("No one was eliminated.")
        }
    }

    private fun checkWinCondition(): Boolean {
        if (!game.checkWinCondition()) return false

        when (game.winner) {
            Winner.VILLAGERS -> interaction.announce("\n*** VILLAGERS WIN! All werewolves are eliminated. ***")
            Winner.WEREWOLVES -> interaction.announce("\n*** WEREWOLVES WIN! They outnumber the villagers. ***")
            null -> {}
        }
        return true
    }
}
