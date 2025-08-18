package org.kopytsia.urlshortener.service

import java.time.Instant

interface JwtTokenService {
    fun generateToken(email: String): String
    fun extractUsername(token: String): String
    fun isTokenValid(token: String): Boolean
    fun getExpiration(token: String): Instant
}
