package com.afonsobenedito.werewolf.console.interaction

import com.afonsobenedito.werewolf.core.model.Player
import java.util.Scanner

class ConsoleGameInteraction : GameInteraction {
    private val scanner = Scanner(System.`in`)

    companion object {
        private const val MINIMUM_OF_PLAYERS = 4
    }

    override fun clearScreen() { repeat(50) { println() } }

    override fun promptEnter() {
        println("Press Enter to continue...")
        scanner.nextLine()
    }

    override fun announce(message: String) { println(message) }

    override fun getPlayerSelection(candidates: List<Player>, prompt: String, allowSkip: Boolean): Player? {
        if (candidates.isEmpty()) return null
        
        println(prompt)
        candidates.forEachIndexed { index, player -> 
            println("${index + 1}. ${player.name}")
        }
        if (allowSkip) println("0. Skip Vote")

        while (true) {
            print("Select number: ")
            val input = scanner.nextLine().trim()
            val selection = input.toIntOrNull()

            if (selection != null) {
                if (allowSkip && selection == 0) return null
                if (selection in 1..candidates.size) {
                    return candidates[selection - 1]
                }
            }
            println("Invalid selection.")
        }
    }

    override fun setupPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        println("Enter the number of players (Minimum $MINIMUM_OF_PLAYERS):")
        var numPlayers = 0
        while (numPlayers < MINIMUM_OF_PLAYERS) {
            print("> ")
            val input = scanner.nextLine()
            val count = input.toIntOrNull()

            if (count != null && count >= MINIMUM_OF_PLAYERS) {
                numPlayers = count
            } else {
                 println("Please enter a valid number (at least $MINIMUM_OF_PLAYERS).")
            }
        }

        for (i in 1..numPlayers) {
            while (true) {
                println("Enter name for Player $i:")
                print("> ")
                val nameInput = scanner.nextLine().trim()
                val name = if (nameInput.isNotEmpty()) nameInput else "Player $i"
                
                if (players.any { it.name == name }) {
                    println("Name '$name' is already taken. Please choose another name.")
                } else {
                    players.add(Player(name))
                    break
                }
            }
        }
        return players
    }
}
