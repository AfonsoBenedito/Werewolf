package com.afonsobenedito

import com.afonsobenedito.werewolf.console.interaction.ConsoleGameInteraction
import com.afonsobenedito.werewolf.console.runner.ConsoleGameRunner

fun main() {
    val interaction = ConsoleGameInteraction()
    val game = Game("Werewolf")
    val runner = ConsoleGameRunner(game, interaction)
    
    runner.run()
}