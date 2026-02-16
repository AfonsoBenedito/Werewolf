package com.afonsobenedito.werewolf.console.interaction

import com.afonsobenedito.werewolf.core.model.Player

interface GameInteraction {
    fun clearScreen()
    fun promptEnter()
    fun announce(message: String)
    fun getPlayerSelection(candidates: List<Player>, prompt: String, allowSkip: Boolean = false): Player?
    fun setupPlayers(): List<Player>
}
