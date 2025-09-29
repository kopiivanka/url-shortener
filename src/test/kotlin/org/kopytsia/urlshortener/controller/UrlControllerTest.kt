package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.junit5.MockKExtension
import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.UrlShortenService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException
import java.security.Principal
import java.util.*

@ExtendWith(MockKExtension::class)
class UrlControllerTest {
    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()

    @MockK
    lateinit var urlShortenService: UrlShortenService
    @MockK
    lateinit var userRepository: UserRepository
    @MockK
    lateinit var jwtTokenService: JwtTokenService
    @MockK
    lateinit var tokenBlacklistService: TokenBlacklistService
    @MockK
    lateinit var userDetailsService: org.springframework.security.core.userdetails.UserDetailsService

    @InjectMockKs
    lateinit var controller: UrlController

    private fun p(email: String) = Principal { email }

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `test 201 and code`() {
        val u = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)
        every { userRepository.findByEmail("me@example.com") } returns Optional.of(u)
        every { urlShortenService.shorten("https://ex.com", u, null, null) } returns "abc123"

        val req = UrlShortenRequest(originalUrl = "https://ex.com")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.code").value("abc123"))
    }

    @Test
    fun `test 401 when user missing`() {
        every { userRepository.findByEmail("nope@example.com") } returns Optional.empty()

        val req = UrlShortenRequest(originalUrl = "https://ex.com")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("nope@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `test 400 from service is propagated`() {
        val u = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)
        every { userRepository.findByEmail("me@example.com") } returns Optional.of(u)
        every { urlShortenService.shorten("bad", u, null, null) } throws ResponseStatusException(HttpStatus.BAD_REQUEST)

        val req = UrlShortenRequest(originalUrl = "bad")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test 409 from service is propagated`() {
        val u = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)
        every { userRepository.findByEmail("me@example.com") } returns Optional.of(u)
        every { urlShortenService.shorten("https://ex.com", u, null, "dup") } throws ResponseStatusException(HttpStatus.CONFLICT)

        val req = UrlShortenRequest(originalUrl = "https://ex.com", customCode = "dup")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isConflict)
    }
}