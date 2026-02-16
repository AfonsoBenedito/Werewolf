package com.afonsobenedito.werewolf.testdouble

import com.afonsobenedito.werewolf.console.interaction.GameInteraction
import com.afonsobenedito.werewolf.core.model.Player

class FakeGameInteraction(
    private val playersToSetup: List<Player>,
    private val playerSelections: MutableList<Player?> = mutableListOf()
) : GameInteraction {

    val announcements = mutableListOf<String>()

    override fun clearScreen() {
        // No-op for test
    }

    override fun promptEnter() {
        // No-op for test
    }

    override fun announce(message: String) {
        announcements.add(message)
    }

    override fun getPlayerSelection(candidates: List<Player>, prompt: String, allowSkip: Boolean): Player? {
        if (playerSelections.isEmpty()) {
            return null // Default to null/skip if ran out of scripted inputs
        }
        return playerSelections.removeAt(0)
    }

    override fun setupPlayers(): List<Player> {
        return playersToSetup
    }
    
    fun queuePlayerSelection(player: Player?) {
        playerSelections.add(player)
    }
}
