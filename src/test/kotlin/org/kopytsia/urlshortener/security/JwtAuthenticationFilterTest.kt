package org.kopytsia.urlshortener.security

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER_EMAIL
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import kotlin.test.*

@ExtendWith(MockKExtension::class)
class JwtAuthenticationFilterTest {

    @MockK
    lateinit var jwt: JwtTokenService
    @MockK
    lateinit var uds: UserDetailsService
    @MockK
    lateinit var blacklist: TokenBlacklistService

    private lateinit var filter: JwtAuthenticationFilter

    @BeforeEach
    fun setUp() {
        filter = JwtAuthenticationFilter(jwt, uds, blacklist, publicEndpoints = listOf("/api/auth/**", "/r/**"))
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() = SecurityContextHolder.clearContext()

    @Test
    fun `bearer token sets authentication`() {
        val token = "abc.def.ghi"
        val email = USER_EMAIL
        val req = MockHttpServletRequest().apply { addHeader(AUTHORIZATION, "Bearer $token") }
        val resp = MockHttpServletResponse()
        val chain = FlagChain()

        every { jwt.isTokenValid(token) } returns true
        every { blacklist.isBlacklisted(token) } returns false
        every { jwt.extractUsername(token) } returns email
        every { uds.loadUserByUsername(email) } returns User.withUsername(email).password("N/A").roles("USER").build()

        filter.doFilter(req, resp, chain)

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals(email, auth.name)
        assertTrue(chain.called)

        verify {
            jwt.isTokenValid(token)
            blacklist.isBlacklisted(token)
            jwt.extractUsername(token)
            uds.loadUserByUsername(email)
        }
    }

    @Test
    fun `no Authorization header - no auth`() {
        val req = MockHttpServletRequest()
        val resp = MockHttpServletResponse()
        val chain = FlagChain()
        filter.doFilter(req, resp, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        assertTrue(chain.called)

        verify { jwt wasNot Called }
        verify { uds wasNot Called }
        verify { blacklist wasNot Called }
    }

    private class FlagChain : FilterChain {
        var called = false
        override fun doFilter(request: ServletRequest?, response: ServletResponse?) { called = true }
    }
}