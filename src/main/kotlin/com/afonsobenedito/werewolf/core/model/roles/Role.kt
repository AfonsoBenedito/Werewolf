package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Villager::class, name = "Villager"),
    JsonSubTypes.Type(value = Wolf::class, name = "Wolf"),
    JsonSubTypes.Type(value = Seer::class, name = "Seer"),
    JsonSubTypes.Type(value = Medic::class, name = "Medic")
)
sealed class Role(
    val name: String,
    val description: String,
) {
    abstract fun nightAction(targetPlayer: Player): NightActionResult
}