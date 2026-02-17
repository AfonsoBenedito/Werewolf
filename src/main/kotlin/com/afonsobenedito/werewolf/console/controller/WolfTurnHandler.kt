package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.console.interaction.GameInteraction
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.Wolf
import com.afonsobenedito.werewolf.core.model.strategy.ConsensusResult
import com.afonsobenedito.werewolf.core.model.strategy.UnanimousWithAbstainStrategy

class WolfTurnHandler : TurnHandler() {
    override val roleName = "Werewolves"
    private val consensusStrategy = UnanimousWithAbstainStrategy()

    override fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction): Player? {
        announceWake(interaction)
        
        var targetPlayer: Player? = null
        if (activePlayers.isNotEmpty()) {
            targetPlayer = getConsensusTarget(activePlayers, allPlayers, interaction)
            if (targetPlayer != null) {
                activePlayers.first().role?.nightAction(targetPlayer)
            }
        } else {
            announceSilence(interaction)
        }

        announceSleep(interaction)
        return targetPlayer
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
                    interaction.announce("The pack is silent. No one will be killed.")
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
            val vote = interaction.getPlayerSelection(possibleTargets, "Select a player to kill:", allowSkip = true)
            
            votes[wolf] = vote
            interaction.promptEnter()
        }
        return votes
    }

}
