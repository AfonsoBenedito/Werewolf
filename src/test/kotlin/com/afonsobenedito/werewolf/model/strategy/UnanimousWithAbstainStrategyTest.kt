package com.afonsobenedito.werewolf.model.strategy

import com.afonsobenedito.werewolf.model.Player
import kotlin.test.Test
import kotlin.test.assertEquals

import kotlin.test.assertTrue

class UnanimousWithAbstainStrategyTest {

    private val strategy = UnanimousWithAbstainStrategy()

    @Test
    fun `getConsensus returns NoTarget when map is empty`() {
        val votes = emptyMap<Player, Player?>()
        val result = strategy.getConsensus(votes)
        assertEquals(ConsensusResult.NoTarget, result)
    }

    @Test
    fun `getConsensus returns NoTarget when all votes are null (abstain)`() {
        val p1 = Player("P1")
        val p2 = Player("P2")
        val votes = mapOf(p1 to null, p2 to null)
        
        val result = strategy.getConsensus(votes)
        assertEquals(ConsensusResult.NoTarget, result)
    }

    @Test
    fun `getConsensus returns Agreed when all non-null votes target the same player`() {
        val p1 = Player("P1")
        val p2 = Player("P2")
        val target = Player("Target")
        
        val votes = mapOf(p1 to target, p2 to target)
        val result = strategy.getConsensus(votes)
        
        assertEquals(ConsensusResult.Agreed(target), result)
    }

    @Test
    fun `getConsensus returns Agreed even with abstentions if others agree`() {
        val p1 = Player("P1")
        val p2 = Player("P2")
        val target = Player("Target")
        
        val votes = mapOf(p1 to target, p2 to null)
        val result = strategy.getConsensus(votes)
        
        assertEquals(ConsensusResult.Agreed(target), result)
    }

    @Test
    fun `getConsensus returns Divided when votes target different players`() {
        val p1 = Player("P1")
        val p2 = Player("P2")
        val t1 = Player("Target1")
        val t2 = Player("Target2")
        
        val votes = mapOf(p1 to t1, p2 to t2)
        val result = strategy.getConsensus(votes)
        
        assertEquals(ConsensusResult.Divided, result)
    }
}
