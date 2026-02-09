package com.afonsobenedito.werewolf.model.roles

import com.afonsobenedito.werewolf.model.Player

class Villager : Role(
    "Villager",
    "Lorem Ipsum"
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        return NightActionResult.NoResult
    }
}