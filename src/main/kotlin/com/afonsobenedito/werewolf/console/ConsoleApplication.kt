package com.afonsobenedito.werewolf.console

import com.afonsobenedito.werewolf.console.interaction.ConsoleGameInteraction
import com.afonsobenedito.werewolf.console.runner.ConsoleGameRunner
import com.afonsobenedito.werewolf.core.Game
import com.afonsobenedito.werewolf.core.GameMode
import java.util.UUID

fun main() {
    val interaction = ConsoleGameInteraction()
    val game = Game(UUID.randomUUID().toString(), "Werewolf", GameMode.OFFLINE)
    val runner = ConsoleGameRunner(game, interaction)
    runner.run()
}
