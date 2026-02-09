package com.afonsobenedito.werewolf.model

import com.afonsobenedito.werewolf.model.roles.NightActionResult
import com.afonsobenedito.werewolf.model.roles.Role

data class Player(
    val name: String,
    var role: Role? = null,
    var isAlive: Boolean = true
) {
    fun die() { isAlive = false }
    fun resurrect() { isAlive = true }
    fun assignRole(newRole: Role) {role = newRole}
}