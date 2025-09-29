package org.kopytsia.urlshortener.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.service.JwtTokenService
import org.springframework.web.server.ResponseStatusException

class JwtTokenServiceImplTest {
    private val secret = "5aA7DQY8Qpq1sdXx3WyS+I3mJ7Fx4dHTZylh0aJvMaE="
    private lateinit var jwtService: JwtTokenService

    @BeforeEach
    fun setUp() {
        val impl = JwtTokenServiceImpl(secret, 15)
        impl.init()
        jwtService = impl
    }

    @Test
    fun `test generateToken and extractUsername`() {
        val email = "user@example.com"
        val token = jwtService.generateToken(email)

        assertNotNull(token)
        assertTrue(jwtService.isTokenValid(token))
        assertEquals(email, jwtService.extractUsername(token))
    }

    @Test
    fun `test isTokenValid returns false`() {
        val invalid = "invalid.token"
        assertFalse(jwtService.isTokenValid(invalid))
        assertThrows(ResponseStatusException::class.java) {
            jwtService.extractUsername(invalid)
        }
    }
}