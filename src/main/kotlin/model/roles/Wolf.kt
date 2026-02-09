package com.afonsobenedito.werewolf.model.roles

import com.afonsobenedito.werewolf.model.Player

class Wolf : Role(
    "Werewolf",
    "Lorem Ipsum"
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        targetPlayer.die()
        return NightActionResult.Kill
    }
}