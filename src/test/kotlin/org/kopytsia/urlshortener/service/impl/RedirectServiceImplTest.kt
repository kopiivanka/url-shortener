package org.kopytsia.urlshortener.service.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.constants.TestConstants.Codes
import org.kopytsia.urlshortener.constants.TestConstants.Urls
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.repository.UrlRepository
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import org.mockito.Mockito.verify
import java.time.OffsetDateTime
import java.util.*

class RedirectServiceImplTest {
    private val urlRepository: UrlRepository = mock()
    private val service = RedirectServiceImpl(urlRepository)

    @Test
    fun `redirect returns url not expired`() {
        val fresh = Url(
            shortCode = Codes.VALID_CUSTOM,
            originalUrl = Urls.VALID,
            owner = USER,
            expiresAt = OffsetDateTime.now().plusDays(1)
        )
        whenever(urlRepository.findByShortCode(Codes.VALID_CUSTOM))
            .thenReturn(Optional.of(fresh))

        val result = service.redirect(Codes.VALID_CUSTOM)
        assertThat(result).contains(fresh)
        verify(urlRepository).findByShortCode(Codes.VALID_CUSTOM)
    }

    @Test
    fun `redirect returns url expired`() {
        val expired = Url(
            shortCode = Codes.DUPLICATE,
            originalUrl = Urls.VALID,
            owner = USER,
            expiresAt = OffsetDateTime.now().minusDays(1)
        )
        whenever(urlRepository.findByShortCode(Codes.DUPLICATE))
            .thenReturn(Optional.of(expired))

        val result = service.redirect(Codes.DUPLICATE)
        assertThat(result).isEmpty
        verify(urlRepository).findByShortCode(Codes.DUPLICATE)
    }

    @Test
    fun `redirect returns empty`() {
        whenever(urlRepository.findByShortCode(Codes.GENERATED))
            .thenReturn(Optional.empty())

        val result = service.redirect(Codes.GENERATED)
        assertThat(result).isEmpty
        verify(urlRepository).findByShortCode(Codes.GENERATED)
    }

    @Test
    fun `redirect expiresAt is null`() {
        val forever = Url(
            shortCode = Codes.INVALID_FORMAT,
            originalUrl = Urls.VALID,
            owner = USER,
            expiresAt = null
        )
        whenever(urlRepository.findByShortCode(Codes.INVALID_FORMAT))
            .thenReturn(Optional.of(forever))

        val result = service.redirect(Codes.INVALID_FORMAT)
        assertThat(result).contains(forever)
        verify(urlRepository).findByShortCode(Codes.INVALID_FORMAT)
    }
}