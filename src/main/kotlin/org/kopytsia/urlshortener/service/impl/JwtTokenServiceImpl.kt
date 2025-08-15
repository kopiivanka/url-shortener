package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.service.JwtTokenService
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import javax.crypto.SecretKey

@Service
class JwtTokenServiceImpl(
    @Value("\${jwt.secret}") private val secret: String,
) : JwtTokenService {

    private lateinit var secretKey: SecretKey
    private val logger = LoggerFactory.getLogger(JwtTokenServiceImpl::class.java)

    @PostConstruct
    fun init() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }

    override fun generateToken(email: String): String {
        return Jwts.builder()
            .setSubject(email)
            .signWith(secretKey)
            .compact()
    }

    override fun extractUsername(token: String): String {
        return try {
            val jwt = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)

            jwt.body.subject
        } catch (e: Exception) {
            logger.warn("JWT parse failed: ${e.message}")
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
}
