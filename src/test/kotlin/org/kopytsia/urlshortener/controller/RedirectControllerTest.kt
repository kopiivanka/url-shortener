package org.kopytsia.urlshortener.controller

import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.controller.external.RedirectController
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.RedirectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(controllers = [RedirectController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(RedirectControllerTest.Config::class)
class RedirectControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var stubUrlService: StubRedirectService

    @MockBean lateinit var jwtTokenService: JwtTokenService
    @MockBean lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean lateinit var userDetailsService: UserDetailsService

    @Test
    fun redirect_found_sets_location_header() {
        stubUrlService.map["go1"] = Url(shortCode = "go1", originalUrl = "https://golang.org")

        mockMvc.perform(get("/r/{code}", "go1"))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", "https://golang.org"))
    }

    @Test
    fun redirect_missing_returns_404() {
        stubUrlService.map.clear()

        mockMvc.perform(get("/r/{code}", "nope"))
            .andExpect(status().isNotFound)
    }

    @TestConfiguration
    class Config {
        @Bean
        fun urlService(): StubRedirectService = StubRedirectService()
    }

    class StubRedirectService : RedirectService {
        val map: MutableMap<String, Url> = mutableMapOf()
        override fun redirect(shortCode: String): Optional<Url> = Optional.ofNullable(map[shortCode])
    }
}
