package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse


class SeerTest {

    @Test
    fun `nightAction returns SeerResult with correct info for Villager`() {
        val seer = Seer()
        val target = Player("Target", role = Villager())
        
        val result = seer.nightAction(target)
        
        // Use manual type checking and casting since NightActionResult is a sealed class
        if (result is NightActionResult.SeerResult) {
            assertEquals("Villager", result.roleName)
            assertFalse(result.hasPowers, "Villager should not have powers")
        } else {
             throw AssertionError("Expected SeerResult but got $result")
        }
    }

    @Test
    fun `nightAction returns SeerResult with correct info for Wolf`() {
        val seer = Seer()
        val target = Player("Target", role = Wolf())
        
        val result = seer.nightAction(target)

        if (result is NightActionResult.SeerResult) {
             assertEquals("Werewolf", result.roleName)
             assertTrue(result.hasPowers, "Wolf should have powers")
        } else {
             throw AssertionError("Expected SeerResult but got $result")
        }
    }

    @Test
    fun `nightAction returns SeerResult with correct info for Medic`() {
        val seer = Seer()
        val target = Player("Target", role = Medic())

        val result = seer.nightAction(target)

        if (result is NightActionResult.SeerResult) {
             assertEquals("Medic", result.roleName)
             assertTrue(result.hasPowers, "Medic should have powers")
        } else {
             throw AssertionError("Expected SeerResult but got $result")
        }
    }
}
