package org.kopytsia.urlshortener.service.impl

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.constants.TestConstants.Codes.VALID_CUSTOM
import org.kopytsia.urlshortener.constants.TestConstants.Ids.URL_ID
import org.kopytsia.urlshortener.constants.TestConstants.Urls.TEST_URL
import org.kopytsia.urlshortener.repository.ClickEventRepository
import org.kopytsia.urlshortener.repository.UrlRepository
import java.util.*
import java.util.Collections.emptyEnumeration

@ExtendWith(MockKExtension::class)
class ClickEventServiceImplTest {

    @MockK(relaxed = true)
    lateinit var clickEventRepository: ClickEventRepository

    @MockK
    lateinit var urlRepository: UrlRepository

    @InjectMockKs
    lateinit var clickEventServiceImpl: ClickEventServiceImpl

    @BeforeEach
    fun setup() {
        clearMocks(clickEventRepository, urlRepository)
        every { clickEventRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `test logAsync saves event when url exists`() {
        val request = mockk<HttpServletRequest>(relaxed = true) {
            every { remoteAddr } returns "127.0.0.1"
            every { headerNames } returns emptyEnumeration()
        }
        every {
            urlRepository.findByShortCode(VALID_CUSTOM)
        } returns Optional.of(TEST_URL)

        clickEventServiceImpl.logAsync(VALID_CUSTOM, request)
        verify { clickEventRepository.save(match { it.codeUuid == URL_ID }) }
    }

    @Test
    fun `test logAsync does nothing when url not found`() {
        every { urlRepository.findByShortCode(VALID_CUSTOM) } returns Optional.empty()

        clickEventServiceImpl.logAsync(
            VALID_CUSTOM,
            mockk<HttpServletRequest>(relaxed = true)
        )
        verify(exactly = 0) { clickEventRepository.save(any()) }
    }

    @Test
    fun `test saves event with minimal metadata`() {
        val request = mockk<HttpServletRequest>(relaxed = true) {
            every { remoteAddr } returns "10.0.0.1"
            every { headerNames } returns emptyEnumeration()
        }
        every {
            urlRepository.findByShortCode(VALID_CUSTOM)
        } returns Optional.of(TEST_URL)
        clickEventServiceImpl.logAsync(VALID_CUSTOM, request)
        verify { clickEventRepository.save(match { "ip" in it.metadata }) }
    }
}