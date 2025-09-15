package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.UrlShortenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.security.Principal
import java.util.*
import org.mockito.Mockito.`when`

@WebMvcTest(controllers = [UrlController::class])
@AutoConfigureMockMvc(addFilters = false)
class UrlControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
) {

    @MockBean lateinit var urlShortenService: UrlShortenService
    @MockBean lateinit var jwtTokenService: JwtTokenService
    @MockBean lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean lateinit var userDetailsService: UserDetailsService
    @MockBean lateinit var userRepository: org.kopytsia.urlshortener.repository.UserRepository

    private fun principalOf(email: String) = Principal { email }

    @Test
    fun `shorten returns 201 and code`() {
        val user = User(
            id = UUID.randomUUID(),
            email = "me@example.com",
            passwordHash = "h",
            role = Role.USER
        )
        `when`(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user))
        `when`(
            urlShortenService.shorten(
                url = "https://example.com/x",
                user = user,
                expiresAt = null,
                code = null
            )
        ).thenReturn("abcDEF12")

        val req = UrlShortenRequest(
            originalUrl = "https://example.com/x",
            expiresAt = null,
            customCode = null
        )

        mockMvc.perform(
            post("/api/url/shorten")
                .principal(principalOf("me@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.code").value("abcDEF12"))
    }
}