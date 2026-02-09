package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.interaction.GameInteraction
import com.afonsobenedito.werewolf.model.Player
import com.afonsobenedito.werewolf.model.roles.Wolf
import com.afonsobenedito.werewolf.model.strategy.ConsensusResult
import com.afonsobenedito.werewolf.model.strategy.UnanimousWithAbstainStrategy

class WolfTurnHandler : TurnHandler() {
    override val roleTitle = "Werewolves"
    private val consensusStrategy = UnanimousWithAbstainStrategy()

    override fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction) {
        announceWake(interaction)
        
        if (activePlayers.isNotEmpty()) {
            val selectedPlayer = getConsensusTarget(activePlayers, allPlayers, interaction)
            if (selectedPlayer != null) {
                performNightAction(activePlayers.first(), selectedPlayer)
            }
        } else {
            announceSilence(interaction)
        }
        
        announceSleep(interaction)
    }

    private fun getConsensusTarget(activeWolves: List<Player>, allPlayers: List<Player>, interaction: GameInteraction): Player? {
        val aliveNonWolves = allPlayers.filter { it.isAlive && it.role !is Wolf }
        var consensusSelectedPlayer: Player? = null
        
        while (consensusSelectedPlayer == null) {
            val votes = collectVotes(activeWolves, aliveNonWolves, interaction)
            
            when (val result = consensusStrategy.getConsensus(votes)) {
                is ConsensusResult.Agreed -> {
                    consensusSelectedPlayer = result.target
                    interaction.clearScreen()
                    interaction.announce("The pack has agreed on a victim.")
                }
                is ConsensusResult.NoTarget -> {
                    interaction.announce("The pack is silent (all abstained). No one will be killed.")
                    return null
                }
                is ConsensusResult.Divided -> {
                    interaction.clearScreen()
                    interaction.announce("The pack is divided. You must agree on a single victim.")
                    interaction.promptEnter()
                }
            }
        }
        return consensusSelectedPlayer
    }

    private fun collectVotes(activeWolves: List<Player>, possibleTargets: List<Player>, interaction: GameInteraction): Map<Player, Player?> {
        val votes = mutableMapOf<Player, Player?>()
        for (wolf in activeWolves) {
            interaction.clearScreen()
            interaction.announce("Werewolf ${wolf.name}, wake up!")
            interaction.announce("Wolves are: ${activeWolves.joinToString { it.name }}")
            val vote = interaction.getPlayerSelection(possibleTargets, "Select a player to eliminate:", allowSkip = true)
            
            votes[wolf] = vote
            interaction.promptEnter()
        }
        return votes
    }

    private fun performNightAction(wolf: Player, target: Player) {
        wolf.role?.nightAction(target)
    }
}
