package org.kopytsia.urlshortener.service.impl

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.constants.TestConstants.Codes
import org.kopytsia.urlshortener.constants.TestConstants.Urls.VALID
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.repository.UrlRepository
import java.time.OffsetDateTime
import java.util.*

class RedirectServiceImplTest {
    private val urlRepository: UrlRepository = mockk()
    private val redirectServiceImpl = RedirectServiceImpl(urlRepository)

    @Test
    fun `test redirect returns url not expired`() {
        val expected = Url(
            shortCode = Codes.VALID_CUSTOM,
            originalUrl = VALID,
            owner = USER,
            expiresAt = OffsetDateTime.now().plusDays(1)
        )
        every { urlRepository.findByShortCode(Codes.VALID_CUSTOM) } returns Optional.of(expected)

        val actual = redirectServiceImpl.getRedirectUrl(Codes.VALID_CUSTOM)

        assertThat(actual).contains(VALID)
        verify { urlRepository.findByShortCode(Codes.VALID_CUSTOM) }
        confirmVerified(urlRepository)
    }

    @Test
    fun `test redirect returns url expired`() {
        val expected = Url(
            shortCode = Codes.DUPLICATE,
            originalUrl = VALID,
            owner = USER,
            expiresAt = OffsetDateTime.now().minusDays(1)
        )
        every { urlRepository.findByShortCode(Codes.DUPLICATE) } returns Optional.of(expected)

        val actual = redirectServiceImpl.getRedirectUrl(Codes.DUPLICATE)

        assertThat(actual).isEmpty
        verify { urlRepository.findByShortCode(Codes.DUPLICATE) }
        confirmVerified(urlRepository)
    }

    @Test
    fun `test redirect returns empty`() {
        every { urlRepository.findByShortCode(Codes.GENERATED) } returns Optional.empty()

        val actual = redirectServiceImpl.getRedirectUrl(Codes.GENERATED)

        assertThat(actual).isEmpty
        verify { urlRepository.findByShortCode(Codes.GENERATED) }
        confirmVerified(urlRepository)
    }

    @Test
    fun `test redirect expiresAt is null`() {
        val expected = Url(
            shortCode = Codes.INVALID_FORMAT,
            originalUrl = VALID,
            owner = USER,
            expiresAt = null
        )
        every { urlRepository.findByShortCode(Codes.INVALID_FORMAT) } returns Optional.of(expected)

        val actual = redirectServiceImpl.getRedirectUrl(Codes.INVALID_FORMAT)

        assertThat(actual).contains(VALID)
        verify { urlRepository.findByShortCode(Codes.INVALID_FORMAT) }
        confirmVerified(urlRepository)
    }
}