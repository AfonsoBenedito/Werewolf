package com.afonsobenedito.werewolf.core.model

import com.afonsobenedito.werewolf.core.model.roles.Role

data class Player(
    val name: String,
    var role: Role? = null,
    var isAlive: Boolean = true
) {
    fun die() { isAlive = false }
    fun resurrect() { isAlive = true }
    fun assignRole(newRole: Role) {role = newRole}
}
