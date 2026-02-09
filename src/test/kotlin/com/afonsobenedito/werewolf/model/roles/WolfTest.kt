package com.afonsobenedito.werewolf.model.roles

import com.afonsobenedito.werewolf.model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WolfTest {

    @Test
    fun `nightAction kills target and returns Kill result`() {
        val wolf = Wolf()
        val target = Player("Target")
        
        val result = wolf.nightAction(target)
        
        assertEquals(NightActionResult.Kill, result)
        assertFalse(target.isAlive, "Target should be dead after Wolf's nightAction")
    }
}
