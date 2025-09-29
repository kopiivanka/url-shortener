package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.dto.request.AuthRequest
import org.kopytsia.urlshortener.dto.request.RegisterRequest
import org.kopytsia.urlshortener.dto.response.AuthResponse
import org.kopytsia.urlshortener.service.AuthService
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class AuthControllerTest {

    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()

    @MockK lateinit var authService: AuthService
    @MockK lateinit var jwtTokenService: JwtTokenService
    @MockK lateinit var tokenBlacklistService: TokenBlacklistService
    @MockK lateinit var userDetailsService: UserDetailsService

    @InjectMockKs
    lateinit var controller: AuthController

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `test register returns created with token`() {
        val req = RegisterRequest("new@example.com", "pw")
        every { authService.register(req) } returns AuthResponse("token123")

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value("token123"))
    }

    @Test
    fun `test login returns ok with token`() {
        val req = AuthRequest("user@example.com", "pw")
        every { authService.login(req) } returns AuthResponse("jwt-abc")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value("jwt-abc"))
    }
}