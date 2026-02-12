package com.afonsobenedito.werewolf.console.controller

import com.afonsobenedito.werewolf.console.interaction.GameInteraction
import com.afonsobenedito.werewolf.core.model.Player

abstract class TurnHandler {
    abstract val roleTitle: String

    abstract fun handleTurn(activePlayers: List<Player>, allPlayers: List<Player>, interaction: GameInteraction): Player?

    protected open fun announceWake(interaction: GameInteraction) {
        interaction.clearScreen()
        interaction.announce("The $roleTitle wakes up")
    }

    protected open fun announceSleep(interaction: GameInteraction) {
        interaction.clearScreen()
        interaction.announce("The $roleTitle goes to sleep...")
    }

    protected fun announceSilence(interaction: GameInteraction) {
        interaction.announce("The $roleTitle is silent...")
        Thread.sleep(2000)
    }
}
