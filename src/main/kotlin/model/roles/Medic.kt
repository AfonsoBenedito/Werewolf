package com.afonsobenedito.werewolf.model.roles

import com.afonsobenedito.werewolf.model.Player

class Medic : Role(
    "Medic",
    "Lorem Ipsum."
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        targetPlayer.resurrect()
        return NightActionResult.Heal
    }
}