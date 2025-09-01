package org.kopytsia.urlshortener.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class JwtAuthenticationFilterTest {

    private lateinit var jwtService: JwtTokenService
    private lateinit var userDetailsService: UserDetailsService
    private lateinit var blacklistService: TokenBlacklistService
    private lateinit var filter: JwtAuthenticationFilter

    @BeforeEach
    fun setUp() {
        jwtService = mock(JwtTokenService::class.java)
        userDetailsService = mock(UserDetailsService::class.java)
        blacklistService = mock(TokenBlacklistService::class.java)
        filter = JwtAuthenticationFilter(jwtService, userDetailsService, blacklistService)
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sets authentication when bearer token present`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = RecordingFilterChain()

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi")
        `when`(jwtService.isTokenValid("abc.def.ghi")).thenReturn(true)
        `when`(blacklistService.isBlacklisted("abc.def.ghi")).thenReturn(false)
        `when`(jwtService.extractUsername("abc.def.ghi")).thenReturn("user@example.com")
        val user: UserDetails = User.builder()
            .username("user@example.com")
            .password("N/A")
            .roles("USER")
            .build()
        `when`(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(user)

        filter.doFilter(request, response, chain)

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals("user@example.com", auth.name)
        assertTrue(chain.invoked)
    }

    @Test
    fun `does not set authentication when header missing`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = RecordingFilterChain()

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        assertTrue(chain.invoked)
        Mockito.verifyNoInteractions(jwtService, userDetailsService, blacklistService)
    }

    private class RecordingFilterChain : FilterChain {
        var invoked: Boolean = false
        override fun doFilter(request: ServletRequest?, response: ServletResponse?) {
            invoked = true
        }
    }
}