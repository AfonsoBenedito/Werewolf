package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

class Wolf : Role(
    "Werewolf",
    "Lurks in the shadows. Chooses a victim each night."
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        return NightActionResult.Kill
    }
}