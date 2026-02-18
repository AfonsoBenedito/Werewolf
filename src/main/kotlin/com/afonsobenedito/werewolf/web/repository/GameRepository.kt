package com.afonsobenedito.werewolf.web.repository

import com.afonsobenedito.werewolf.core.Game
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class GameRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(GameRepository::class.java)

    companion object {
        private const val GAME_KEY_PREFIX = "game:"
        private const val GAME_TTL_MINUTES = 120L
        private const val SHORT_TTL_MINUTES = 10L
    }

    fun save(game: Game) {
        val json = objectMapper.writeValueAsString(game)
        redisTemplate.opsForValue().set(
            GAME_KEY_PREFIX + game.id,
            json,
            GAME_TTL_MINUTES,
            TimeUnit.MINUTES
        )
    }

    fun load(gameId: String): Game? {
        val json = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId) ?: return null
        return try {
            objectMapper.readValue(json, Game::class.java)
        } catch (e: Exception) {
            logger.error("Failed to deserialize game {}", gameId, e)
            null
        }
    }

    fun setShortTtl(gameId: String) {
        redisTemplate.expire(GAME_KEY_PREFIX + gameId, SHORT_TTL_MINUTES, TimeUnit.MINUTES)
    }
}
