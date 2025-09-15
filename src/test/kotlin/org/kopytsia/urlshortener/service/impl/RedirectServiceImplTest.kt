package org.kopytsia.urlshortener.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.repository.UrlRepository
import org.mockito.Mockito.*
import java.time.OffsetDateTime
import java.util.*

class RedirectServiceImplTest {

    private val urlRepository: UrlRepository = mock(UrlRepository::class.java)
    private val service = RedirectServiceImpl(urlRepository)

    @Test
    fun `resolve returns present when not expired`() {
        val fresh = Url(shortCode = "abc", originalUrl = "https://example.com", expiresAt = OffsetDateTime.now().plusDays(1))
        `when`(urlRepository.findByShortCode("abc")).thenReturn(Optional.of(fresh))

        val result = service.redirect("abc")

        assertTrue(result.isPresent)
        assertEquals(fresh, result.get())
        verify(urlRepository, times(1)).findByShortCode("abc")
        verifyNoMoreInteractions(urlRepository)
    }

    @Test
    fun `resolve filters out expired url`() {
        val expired = Url(shortCode = "old", originalUrl = "https://old.example", expiresAt = OffsetDateTime.now().minusDays(1))
        `when`(urlRepository.findByShortCode("old")).thenReturn(Optional.of(expired))

        val result = service.redirect("old")

        assertTrue(result.isEmpty)
        verify(urlRepository, times(1)).findByShortCode("old")
        verifyNoMoreInteractions(urlRepository)
    }

    @Test
    fun `resolve returns empty when not found`() {
        `when`(urlRepository.findByShortCode("nope")).thenReturn(Optional.empty())

        val result = service.redirect("nope")

        assertTrue(result.isEmpty)
        verify(urlRepository, times(1)).findByShortCode("nope")
        verifyNoMoreInteractions(urlRepository)
    }
}