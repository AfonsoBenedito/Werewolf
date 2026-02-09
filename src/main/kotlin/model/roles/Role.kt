package com.afonsobenedito.werewolf.model.roles

import com.afonsobenedito.werewolf.model.Player

sealed class Role(
    val name: String,
    val description: String,
) {
    abstract fun nightAction(targetPlayer: Player): NightActionResult
}