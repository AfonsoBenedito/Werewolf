package com.afonsobenedito.werewolf.core.model.strategy

import com.afonsobenedito.werewolf.core.model.Player

class UnanimousWithAbstainStrategy : ConsensusStrategy {
    override fun getConsensus(votes: Map<Player, Player?>): ConsensusResult {
        val uniqueTargets = votes.values.filterNotNull().toSet()

        return when {
            uniqueTargets.isEmpty() -> ConsensusResult.NoTarget
            uniqueTargets.size == 1 -> ConsensusResult.Agreed(uniqueTargets.first())
            else -> ConsensusResult.Divided
        }
    }
}
