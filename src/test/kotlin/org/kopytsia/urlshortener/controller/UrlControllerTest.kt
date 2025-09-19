package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.UrlShortenService
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.security.Principal
import java.util.*

@WebMvcTest(controllers = [UrlController::class])
@AutoConfigureMockMvc(addFilters = false)
class UrlControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var objectMapper: ObjectMapper
    @MockBean
    lateinit var urlShortenService: UrlShortenService
    @MockBean
    lateinit var userRepository: UserRepository
    @MockBean
    lateinit var jwtTokenService: JwtTokenService
    @MockBean
    lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean
    lateinit var userDetailsService: UserDetailsService

    private fun p(email: String) = Principal { email }

    @Test
    fun `test 201 and code`() {
        val u = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)
        `when`(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(u))
        `when`(urlShortenService.shorten("https://ex.com", u, null, null)).thenReturn("abc123")

        val req = UrlShortenRequest(originalUrl = "https://ex.com")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.code").value("abc123"))
    }

    @Test
    fun `test 401 when user missing`() {
        `when`(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty())

        val req = UrlShortenRequest(originalUrl = "https://ex.com")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("nope@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `test 400 from service is propagated`() {
        val u = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)
        `when`(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(u))
        `when`(urlShortenService.shorten("bad", u, null, null))
            .thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST))

        val req = UrlShortenRequest(originalUrl = "bad")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `test 409 from service is propagated`() {
        val u = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)
        `when`(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(u))
        `when`(urlShortenService.shorten("https://ex.com", u, null, "dup"))
            .thenThrow(ResponseStatusException(HttpStatus.CONFLICT))

        val req = UrlShortenRequest(originalUrl = "https://ex.com", customCode = "dup")

        mockMvc.perform(
            post("/api/url/shorten").principal(p("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isConflict)
    }
}