package com.afonsobenedito.werewolf.console

import com.afonsobenedito.werewolf.console.interaction.ConsoleGameInteraction
import com.afonsobenedito.werewolf.console.runner.ConsoleGameRunner
import com.afonsobenedito.werewolf.core.Game

fun main() {
    val interaction = ConsoleGameInteraction()
    val game = Game("Werewolf")
    val runner = ConsoleGameRunner(game, interaction)
    runner.run()
}
