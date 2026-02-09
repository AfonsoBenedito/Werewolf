package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

class Wolf : Role(
    "Werewolf",
    "Lorem Ipsum"
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        return NightActionResult.Kill
    }
}