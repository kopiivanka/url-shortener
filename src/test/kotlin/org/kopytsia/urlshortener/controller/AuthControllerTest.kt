package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.dto.request.AuthRequest
import org.kopytsia.urlshortener.dto.request.RegisterRequest
import org.kopytsia.urlshortener.dto.response.AuthResponse
import org.kopytsia.urlshortener.service.AuthService
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AuthController::class])
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var objectMapper: ObjectMapper
    @MockBean
    lateinit var authService: AuthService
    @MockBean
    lateinit var jwtTokenService: JwtTokenService
    @MockBean
    lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean
    lateinit var userDetailsService: UserDetailsService

    @Test
    fun `test register returns created with token`() {
        val req = RegisterRequest("new@example.com", "pw")
        `when`(authService.register(req)).thenReturn(AuthResponse("token123"))

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.token").value("token123")
            )
    }

    @Test
    fun `test login returns ok with token`() {
        val req = AuthRequest("user@example.com", "pw")
        `when`(authService.login(req)).thenReturn(AuthResponse("jwt-abc"))

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.token").value("jwt-abc")
            )
    }
}