package org.kopytsia.urlshortener.service

interface TokenBlacklistService {
    fun blacklist(token: String, ttlSeconds: Long)
    fun isBlacklisted(token: String): Boolean
}