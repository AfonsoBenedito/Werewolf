package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.console.interaction.GameInteraction
import com.afonsobenedito.werewolf.core.model.Player

abstract class TurnHandler {
    abstract val roleName: String

    abstract fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction): Player?

    protected open fun announceWake(interaction: GameInteraction) {
        interaction.clearScreen()
        interaction.announce("The $roleName wakes up")
    }

    protected open fun announceSleep(interaction: GameInteraction) {
        interaction.clearScreen()
        interaction.announce("The $roleName goes to sleep...")
        interaction.promptEnter()
    }

    protected fun announceSilence(interaction: GameInteraction) {
        interaction.announce("The $roleName is silent...")
        Thread.sleep(2000)
    }
}
