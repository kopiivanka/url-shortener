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
import org.kopytsia.urlshortener.service.RedirectService
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
    lateinit var redirectService: RedirectService
    @InjectMockKs
    lateinit var redirectController: RedirectController

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(redirectController).build()
    }

    @Test
    fun `test found 302 with location`() {
        every { redirectService.getRedirectUrl(VALID_CUSTOM) } returns Optional.of(VALID)

        mockMvc.perform(get("/r/{code}", VALID_CUSTOM))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", VALID))
    }

    @Test
    fun `test missing 404`() {
        every { redirectService.getRedirectUrl(GENERATED) } returns Optional.empty()

        mockMvc.perform(get("/r/{code}", GENERATED))
            .andExpect(status().isNotFound)
    }
}