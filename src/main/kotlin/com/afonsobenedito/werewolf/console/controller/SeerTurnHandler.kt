package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.console.interaction.GameInteraction
import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.NightActionResult

class SeerTurnHandler : TurnHandler() {
    override val roleTitle = "Seer"

    override fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction): Player? {
        announceWake(interaction)
        
        val seer = activePlayers.firstOrNull()
        if (seer != null) {
            handleSeerAction(seer, allPlayers, interaction)
        } else {
            announceSilence(interaction)
        }
        
        announceSleep(interaction)
        return null // Seer doesn't target for death/save
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
