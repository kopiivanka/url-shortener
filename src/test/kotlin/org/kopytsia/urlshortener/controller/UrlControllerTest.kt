package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.UrlShortenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.security.Principal
import java.time.OffsetDateTime
import java.util.*

@WebMvcTest(controllers = [UrlController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(UrlControllerTest.Config::class)
class UrlControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var stubShortCodeService: StubShortCodeService

    @MockBean lateinit var jwtTokenService: JwtTokenService
    @MockBean lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean lateinit var userDetailsService: UserDetailsService

    private fun principalOf(email: String) = Principal { email }

    @Test
    fun shorten_returns_created_with_body() {
        val now = OffsetDateTime.now()
        val owner = User(id = UUID.randomUUID(), email = "me@example.com", passwordHash = "h", role = Role.USER)
        val saved = Url(
            id = UUID.randomUUID(),
            shortCode = "abcDEF12",
            originalUrl = "https://example.com/x",
            owner = owner,
            createdAt = now,
            expiresAt = now.plusDays(1)
        )
        stubShortCodeService.nextShorten = saved

        val req = UrlShortenRequest(
            originalUrl = "https://example.com/x",
            expiresAt = saved.expiresAt,
            customCode = null
        )

        mockMvc.perform(
            post("/api/url/shorten")
                .principal(principalOf("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.shortCode").value("abcDEF12"))
            .andExpect(jsonPath("$.originalUrl").value("https://example.com/x"))
            .andExpect(jsonPath("$.ownerId").value(owner.id.toString()))
    }

    @TestConfiguration
    class Config {
        @Bean
        fun urlShortCodeService(): StubShortCodeService = StubShortCodeService()
    }

    class StubShortCodeService : UrlShortenService {
        var nextShorten: Url? = null
        override fun shorten(
            originalUrl: String,
            ownerEmail: String?,
            expiresAt: OffsetDateTime?,
            customCode: String?
        ): Url = nextShorten ?: error("nextShorten not set")
    }
}