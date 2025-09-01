package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.UrlService
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.security.Principal
import java.time.OffsetDateTime
import java.util.*

@WebMvcTest(controllers = [UrlController::class])
@AutoConfigureMockMvc(addFilters = false)
class UrlControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @MockBean lateinit var urlService: UrlService
    @MockBean lateinit var jwtTokenService: JwtTokenService
    @MockBean lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean lateinit var userDetailsService: UserDetailsService

    private fun principalOf(email: String) = Principal { email }

    @Test
    fun shorten_returns_created_with_body() {
        val now = OffsetDateTime.now()
        val user = User(id = UUID.randomUUID(), email = "me@example.com", passwordHash = "h", role = Role.USER)
        val saved = Url(
            id = UUID.randomUUID(), shortCode = "abcDEF12", originalUrl = "https://example.com/x",
            owner = user, createdAt = now, expiresAt = now.plusDays(1)
        )

        val req = UrlShortenRequest(originalUrl = "https://example.com/x", expiresAt = saved.expiresAt, customCode = null)
        `when`(urlService.shortenForUser("me@example.com", req.originalUrl, req.expiresAt, req.customCode)).thenReturn(saved)

        mockMvc.perform(
            post("/api/url/shorten")
                .principal(principalOf("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.shortCode").value("abcDEF12"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.originalUrl").value("https://example.com/x"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.ownerId").value(user.id.toString()))
    }

    @Test
    fun redirect_found_sets_location_header() {
        val url = Url(shortCode = "go1", originalUrl = "https://golang.org")
        `when`(urlService.resolve("go1")).thenReturn(Optional.of(url))

        mockMvc.perform(get("/r/{code}", "go1"))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", "https://golang.org"))
    }

    @Test
    fun redirect_missing_returns_404() {
        `when`(urlService.resolve("nope")).thenReturn(Optional.empty())

        mockMvc.perform(get("/r/{code}", "nope"))
            .andExpect(status().isNotFound)
    }
}

