package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.service.JwtTokenService
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtTokenServiceImpl(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-minutes:15}") private val expirationMinutes: Long,
) : JwtTokenService {

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }

    override fun generateToken(email: String): String {
        val now = Instant.now()
        val exp = now.plus(expirationMinutes, ChronoUnit.MINUTES)
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .signWith(secretKey)
            .compact()
    }

    override fun extractUsername(token: String): String {
        try {
            val jwt = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)

            return jwt.body.subject
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token")
        }
    }

    override fun isTokenValid(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (_: JwtException) {
            false
        }
    }

    override fun getExpiration(token: String): Instant {
        try {
            val jwt = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            return jwt.body.expiration.toInstant()
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token")
        }
    }
}