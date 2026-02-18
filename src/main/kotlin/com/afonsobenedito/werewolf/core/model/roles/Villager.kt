package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

class Villager : Role(
    "Villager",
    "An ordinary villager. No special powers, but a powerful vote."
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        return NightActionResult.NoResult
    }
}