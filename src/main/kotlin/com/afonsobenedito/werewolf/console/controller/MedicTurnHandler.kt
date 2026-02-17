package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.console.interaction.GameInteraction
import com.afonsobenedito.werewolf.core.model.Player

class MedicTurnHandler : TurnHandler() {
    override val roleName = "Medic"

    override fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction): Player? {
        announceWake(interaction)

        var protectedPlayer: Player? = null
        val medic = activePlayers.firstOrNull()
        if (medic != null) {
            protectedPlayer = handleMedicAction(medic, allPlayers, interaction)
        } else {
            announceSilence(interaction)
        }

        announceSleep(interaction)
        return protectedPlayer
    }

    private fun handleMedicAction(medic: Player, allPlayers: List<Player>, interaction: GameInteraction): Player? {
        interaction.announce("Medic: ${medic.name}")
        val selectedPlayer = interaction.getPlayerSelection(allPlayers, "Choose a player to heal:", allowSkip = true)
        if (selectedPlayer != null) {
            medic.role?.nightAction(selectedPlayer)
            return selectedPlayer
        }
        return null
    }
}
