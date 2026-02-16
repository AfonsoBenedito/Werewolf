package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.NightActionResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WolfTest {

    @Test
    fun `nightAction kills target and returns Kill result`() {
        val wolf = Wolf()
        val target = Player("Target")
        
        val result = wolf.nightAction(target)
        
        assertEquals(NightActionResult.Kill, result)
        assertTrue(target.isAlive, "Target should be alive after Wolf's nightAction (death is deferred)")
    }
}
