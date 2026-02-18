package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

class Medic : Role(
    "Medic",
    "Sworn to protect. Saves one player from death each night."
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        targetPlayer.resurrect()
        return NightActionResult.Heal
    }
}