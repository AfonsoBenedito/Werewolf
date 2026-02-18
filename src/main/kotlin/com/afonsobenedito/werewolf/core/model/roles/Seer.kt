package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

class Seer : Role(
    "Seer",
    "Gifted with foresight. Inspects one player each night."
) {
    override fun nightAction(targetPlayer: Player): NightActionResult {
        val hasPowers = targetPlayer.role !is Villager
        return NightActionResult.SeerResult(targetPlayer.role?.name ?: "Unknown", hasPowers)
    }
}