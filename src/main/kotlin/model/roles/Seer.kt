package com.afonsobenedito.werewolf.model.roles

import com.afonsobenedito.werewolf.model.Player

class Seer : Role(
    "Seer",
    "Lorem Ipsum"
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        val hasPowers = targetPlayer.role !is Villager
        return NightActionResult.SeerResult(targetPlayer.role?.name ?: "Unknown", hasPowers)
    }
}