package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

private const val BLACKLIST_PREFIX = "jwt:blacklist:"

@Service
class RedisTokenBlacklistService(
    private val redisTemplate: StringRedisTemplate
) : TokenBlacklistService {
    override fun blacklist(token: String, ttlSeconds: Long) {
        val key = BLACKLIST_PREFIX + token
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS)
        } else {
            redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.SECONDS)
        }
    }

    override fun isBlacklisted(token: String): Boolean {
        val key = BLACKLIST_PREFIX + token
        return redisTemplate.hasKey(key) == true
    }
}