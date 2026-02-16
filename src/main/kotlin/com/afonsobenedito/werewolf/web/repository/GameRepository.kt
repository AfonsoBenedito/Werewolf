package com.afonsobenedito.werewolf.web.repository

import com.afonsobenedito.werewolf.core.Game
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
open class GameRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val GAME_KEY_PREFIX = "game:"
        private const val GAME_TTL_MINUTES = 120L // 2 hours
        private const val SHORT_TTL_MINUTES = 10L // 10 minutes for finished games
    }

    open fun save(game: Game?) {
        if (game == null) return
        val json = objectMapper.writeValueAsString(game)
        redisTemplate.opsForValue().set(
            GAME_KEY_PREFIX + game.id,
            json,
            GAME_TTL_MINUTES,
            TimeUnit.MINUTES
        )
    }

    open fun load(gameId: String): Game? {
        val json = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId) ?: return null
        return try {
            objectMapper.readValue(json, Game::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    open fun delete(gameId: String) {
        redisTemplate.delete(GAME_KEY_PREFIX + gameId)
    }

    open fun setShortTtl(gameId: String) {
        redisTemplate.expire(GAME_KEY_PREFIX + gameId, SHORT_TTL_MINUTES, TimeUnit.MINUTES)
    }
    
    open fun exists(gameId: String): Boolean {
        return redisTemplate.hasKey(GAME_KEY_PREFIX + gameId)
    }
}
