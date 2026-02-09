package com.afonsobenedito.werewolf.model.strategy

import com.afonsobenedito.werewolf.model.Player

interface ConsensusStrategy {
    fun getConsensus(votes: Map<Player, Player?>): ConsensusResult
}

sealed class ConsensusResult {
    data class Agreed(val target: Player) : ConsensusResult()
    object NoTarget : ConsensusResult()
    object Divided : ConsensusResult()
}
