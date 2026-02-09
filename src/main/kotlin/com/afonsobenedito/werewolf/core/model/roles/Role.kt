package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

sealed class Role(
    val name: String,
    val description: String,
) {
    abstract fun nightAction(targetPlayer: Player): NightActionResult
}