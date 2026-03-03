package com.afonsobenedito.werewolf.web.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class SessionRepository(private val redisTemplate: StringRedisTemplate) {

    companion object {
        private const val SESSION_TTL_MINUTES = 120L
    }

    fun save(token: String, gameId: String, playerName: String) {
        redisTemplate.opsForValue().set(
            sessionKey(token, gameId),
            playerName,
            SESSION_TTL_MINUTES,
            TimeUnit.MINUTES
        )
    }

    fun findPlayerName(token: String, gameId: String): String? =
        redisTemplate.opsForValue().get(sessionKey(token, gameId))

    private fun sessionKey(token: String, gameId: String) = "session:$token:$gameId"
}
