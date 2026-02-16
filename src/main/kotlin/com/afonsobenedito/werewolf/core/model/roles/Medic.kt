package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

class Medic : Role(
    "Medic",
    "Lorem Ipsum."
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        targetPlayer.resurrect()
        return NightActionResult.Heal
    }
}