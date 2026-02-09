package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.interaction.GameInteraction
import com.afonsobenedito.werewolf.model.Player

abstract class TurnHandler {
    abstract val roleTitle: String

    abstract fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction)

    protected fun announceWake(interaction: GameInteraction) {
        interaction.clearScreen()
        interaction.announce("$roleTitle, wake up!")
    }

    protected fun announceSleep(interaction: GameInteraction) {
        interaction.clearScreen()
        interaction.announce("$roleTitle, go to sleep.")
    }

    protected fun announceSilence(interaction: GameInteraction) {
        interaction.announce("$roleTitle is silent...")
        Thread.sleep(2000)
    }
}
