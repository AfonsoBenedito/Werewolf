package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.interaction.GameInteraction
import com.afonsobenedito.werewolf.model.Player
import com.afonsobenedito.werewolf.model.roles.NightActionResult

class SeerTurnHandler : TurnHandler() {
    override val roleTitle = "Seer"

    override fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction) {
        announceWake(interaction)
        
        val seer = activePlayers.firstOrNull()
        if (seer != null) {
            handleSeerAction(seer, allPlayers, interaction)
        } else {
            announceSilence(interaction)
        }
        
        announceSleep(interaction)
    }

    private fun handleSeerAction(seer: Player, allPlayers: List<Player>, interaction: GameInteraction) {
        interaction.announce("Seer: ${seer.name}")

        val validTargets = allPlayers.filter { it != seer }  // Filter out self
        val selectedPlayer = interaction.getPlayerSelection(validTargets, "Choose a player to inspect:", allowSkip = true)
        
        if (selectedPlayer != null) {
            val result = seer.role?.nightAction(selectedPlayer)
            if (result is NightActionResult.SeerResult) {
                if (result.hasPowers) {
                    interaction.announce("${selectedPlayer.name} is NOT a regular Villager!")
                } else {
                    interaction.announce("${selectedPlayer.name} is a regular Villager.")
                }
            }
        }
        interaction.promptEnter()
    }
}
