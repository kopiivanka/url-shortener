package org.kopytsia.urlshortener.service

interface JwtTokenService {
    fun generateToken(email: String): String
    fun extractUsername(token: String): String
    fun isTokenValid(token: String): Boolean
}