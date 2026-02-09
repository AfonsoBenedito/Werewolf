package com.afonsobenedito.werewolf.model.roles

import com.afonsobenedito.werewolf.model.Player
import kotlin.test.Test
import kotlin.test.assertEquals

class VillagerTest {

    @Test
    fun `nightAction returns NoResult`() {
        val villager = Villager()
        val target = Player("Target")
        
        val result = villager.nightAction(target)
        
        assertEquals(NightActionResult.NoResult, result)
    }
}
