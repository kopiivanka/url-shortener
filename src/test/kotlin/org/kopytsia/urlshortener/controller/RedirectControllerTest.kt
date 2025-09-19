package org.kopytsia.urlshortener.controller

import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.constants.TestConstants.Codes
import org.kopytsia.urlshortener.constants.TestConstants.Urls
import org.kopytsia.urlshortener.controller.external.RedirectController
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.RedirectService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(controllers = [RedirectController::class])
@AutoConfigureMockMvc(addFilters = false)
class RedirectControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @MockBean
    lateinit var redirectService: RedirectService
    @MockBean
    lateinit var jwtTokenService: JwtTokenService
    @MockBean
    lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean
    lateinit var userDetailsService: UserDetailsService

    @Test
    fun `test found 302 with location`() {
        val code = Codes.VALID_CUSTOM
        val target = Urls.VALID
        given(redirectService.redirect(code)).willReturn(
            Optional.of(Url(shortCode = code, originalUrl = target))
        )

        mockMvc.perform(get("/r/{code}", code))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", target))
    }

    @Test
    fun `test missing 404`() {
        val code = Codes.GENERATED
        given(redirectService.redirect(code)).willReturn(Optional.empty())

        mockMvc.perform(get("/r/{code}", code))
            .andExpect(status().isNotFound)
    }
}