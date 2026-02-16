package com.afonsobenedito.werewolf.core.model.roles

import com.afonsobenedito.werewolf.core.model.Player
import com.afonsobenedito.werewolf.core.model.roles.NightActionResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MedicTest {

    @Test
    fun `nightAction resurrects target and returns Heal result`() {
        val medic = Medic()
        val target = Player("Target", isAlive = false)
        
        val result = medic.nightAction(target)
        
        assertEquals(NightActionResult.Heal, result)
        assertTrue(target.isAlive, "Target should be alive after Medic's nightAction")
    }
}
