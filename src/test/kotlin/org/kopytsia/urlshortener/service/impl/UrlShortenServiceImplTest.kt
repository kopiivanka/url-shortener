package org.kopytsia.urlshortener.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.RandomCodeService
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.*

class UrlShortenServiceImplTest {

    private val urlRepository: UrlRepository = mock(UrlRepository::class.java)
    private val randomCodeService: RandomCodeService = mock(RandomCodeService::class.java)
    private val service = UrlShortenServiceImpl(urlRepository, randomCodeService)

    private val user = User(
        id = UUID.randomUUID(),
        email = "me@example.com",
        passwordHash = "xxx",
        role = Role.USER
    )

    @Test
    fun `custom code`() {
        `when`(urlRepository.existsByShortCode("good123")).thenReturn(false)

        val url = "https://example.com"
        val expiresAt = OffsetDateTime.now().plusDays(1)

        val result = service.shorten(url, user, expiresAt, "good123")

        assertEquals("/r/good123", result)
        verify(urlRepository).save(any(Url::class.java))
    }

    @Test
    fun `code not provided`() {
        `when`(randomCodeService.generate()).thenReturn("gen123")
        `when`(urlRepository.existsByShortCode("gen123")).thenReturn(false)

        val url = "https://example.com"
        val result = service.shorten(url, user, null, null)

        assertEquals("/r/gen123", result)
        verify(urlRepository).save(any(Url::class.java))
    }

    @Test
    fun `invalid url`() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.shorten("ftp://example.com", user, null, "abc123")
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        verifyNoInteractions(urlRepository)
    }

    @Test
    fun `code taken`() {
        `when`(urlRepository.existsByShortCode("dup")).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.shorten("https://example.com", user, null, "dup")
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
        verify(urlRepository, never()).save(any())
    }
}