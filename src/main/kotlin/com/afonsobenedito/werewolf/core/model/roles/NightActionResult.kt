package com.afonsobenedito.werewolf.core.model.roles

sealed class NightActionResult {
    data class SeerResult(val roleName: String, val hasPowers: Boolean) : NightActionResult()
    object Kill : NightActionResult()
    object Heal : NightActionResult()
    object NoResult : NightActionResult()
}
