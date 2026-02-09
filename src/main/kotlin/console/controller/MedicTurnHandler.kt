package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.interaction.GameInteraction
import com.afonsobenedito.werewolf.model.Player

class MedicTurnHandler : TurnHandler() {
    override val roleTitle = "Medic"

    override fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction) {
        announceWake(interaction)

        val medic = activePlayers.firstOrNull()
        if (medic != null) {
            handleMedicAction(medic, allPlayers, interaction)
        } else {
            announceSilence(interaction)
        }

        announceSleep(interaction)
    }

    private fun handleMedicAction(medic: Player, allPlayers: List<Player>, interaction: GameInteraction) {
        interaction.announce("Medic: ${medic.name}")
        val selectedPlayer = interaction.getPlayerSelection(allPlayers, "Choose a player to protect/revive:", allowSkip = true)
        if (selectedPlayer != null) {
            medic.role?.nightAction(selectedPlayer)
        }
    }
}
