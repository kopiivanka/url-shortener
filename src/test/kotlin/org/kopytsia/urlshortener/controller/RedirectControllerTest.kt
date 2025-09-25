package org.kopytsia.urlshortener.controller

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.constants.TestConstants.Codes.GENERATED
import org.kopytsia.urlshortener.constants.TestConstants.Codes.VALID_CUSTOM
import org.kopytsia.urlshortener.constants.TestConstants.Urls.VALID
import org.kopytsia.urlshortener.controller.external.RedirectController
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.RedirectService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

@ExtendWith(MockKExtension::class)
class RedirectControllerTest {
    private lateinit var mockMvc: MockMvc

    @MockK
    lateinit var getRedirectUrlService: RedirectService
    @MockK
    lateinit var jwtTokenService: JwtTokenService
    @MockK
    lateinit var tokenBlacklistService: TokenBlacklistService
    @MockK
    lateinit var userDetailsService: UserDetailsService

    @InjectMockKs
    lateinit var controller: RedirectController

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `test found 302 with location`() {
        every { getRedirectUrlService.getRedirectUrl(VALID_CUSTOM) } returns
                Optional.of(Url(shortCode = VALID_CUSTOM, originalUrl = VALID))

        mockMvc.perform(get("/r/{code}", VALID_CUSTOM))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", VALID))
    }

    @Test
    fun `test missing 404`() {
        every { getRedirectUrlService.getRedirectUrl(GENERATED) } returns Optional.empty()

        mockMvc.perform(get("/r/{code}", GENERATED))
            .andExpect(status().isNotFound)
    }
}