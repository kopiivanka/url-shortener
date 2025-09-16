package org.kopytsia.urlshortener.service.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.RandomCodeService
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class UrlShortenServiceImplTest {

    private val repo: UrlRepository = mock(UrlRepository::class.java)
    private val rnd: RandomCodeService = mock(RandomCodeService::class.java)
    private val service = UrlShortenServiceImpl(repo, rnd)

    private val user = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)

    @Test fun `custom code ok saves and returns redirect`() {
        `when`(repo.existsByShortCode("Good_123")).thenReturn(false)
        `when`(repo.save(any(Url::class.java))).thenAnswer { it.arguments[0] }

        val r = service.shorten("https://ex.com", user, null, "Good_123")
        assertEquals("/r/Good_123", r)
        verify(repo).save(any(Url::class.java))
    }

    @Test fun `generates when code null`() {
        `when`(rnd.generate()).thenReturn("genCode")
        `when`(repo.existsByShortCode("genCode")).thenReturn(false)
        `when`(repo.save(any(Url::class.java))).thenAnswer { it.arguments[0] }

        val r = service.shorten("https://ex.com", user, null, null)
        assertEquals("/r/genCode", r)
        verify(repo).save(any(Url::class.java))
    }

    @Test fun `bad url 400`() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.shorten("ftp://bad", user, null, "abc")
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        verifyNoInteractions(repo, rnd)
    }

    @Test fun `bad code format 400`() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.shorten("https://ex.com", user, null, "bad space")
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        verify(repo, never()).save(any())
    }

    @Test fun `code taken 409`() {
        `when`(repo.existsByShortCode("dup")).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.shorten("https://ex.com", user, null, "dup")
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
        verify(repo, never()).save(any())
    }
}