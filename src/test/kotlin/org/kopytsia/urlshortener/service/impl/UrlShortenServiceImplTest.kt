package org.kopytsia.urlshortener.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.repository.UserRepository
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.*

class UrlShortenServiceImplTest {

    private val urlRepository: UrlRepository = mock(UrlRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val service = UrlShortenServiceImpl(urlRepository, userRepository)

    @Test
    fun `shorten with valid custom code saves entity`() {
        `when`(urlRepository.existsByShortCode("Good_123")).thenReturn(false)
        val captor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(captor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val now = OffsetDateTime.now()
        val result = service.shorten(
            originalUrl = "https://example.com/x",
            ownerEmail = null,
            expiresAt = now.plusDays(1),
            customCode = "Good_123"
        )

        verify(urlRepository, times(1)).existsByShortCode("Good_123")
        verify(urlRepository, times(1)).save(any(Url::class.java))
        verifyNoMoreInteractions(urlRepository)
        verifyNoInteractions(userRepository)

        val saved = captor.value
        assertEquals("Good_123", saved.shortCode)
        assertEquals("https://example.com/x", saved.originalUrl)
        assertNull(saved.owner)
        assertEquals(saved, result)
    }

    @Test
    fun `shorten throws BAD_REQUEST for invalid custom format`() {
        val ex = assertThrows<ResponseStatusException> {
            service.shorten("https://example.com", null, null, "bad space")
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        verifyNoInteractions(urlRepository, userRepository)
    }

    @Test
    fun `shorten throws CONFLICT when custom code taken`() {
        `when`(urlRepository.existsByShortCode("taken")).thenReturn(true)

        val ex = assertThrows<ResponseStatusException> {
            service.shorten("https://example.com", null, null, "taken")
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        verify(urlRepository, times(1)).existsByShortCode("taken")
        verifyNoMoreInteractions(urlRepository)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun `shorten generates 8-char code when no custom`() {
        `when`(urlRepository.existsByShortCode(anyString())).thenReturn(false)
        val captor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(captor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val result = service.shorten("https://example.com/y", null, null, null)

        val saved = captor.value
        assertTrue(saved.shortCode.matches(Regex("^[A-Za-z0-9]{8}$")))
        assertEquals(saved, result)
        verify(urlRepository, atLeastOnce()).existsByShortCode(anyString())
    }

    @Test
    fun `shorten falls back to 12-char when collisions`() {
        `when`(urlRepository.existsByShortCode(anyString())).thenReturn(true)
        val captor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(captor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val result = service.shorten("https://example.com/z", null, null, null)

        val saved = captor.value
        assertEquals(12, saved.shortCode.length)
        assertEquals(saved, result)
        verify(urlRepository, atLeast(10)).existsByShortCode(anyString())
    }

    @Test
    fun `shorten with owner email sets owner`() {
        val user = User(id = UUID.randomUUID(), email = "me@example.com", passwordHash = "h", role = Role.USER)
        `when`(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user))
        `when`(urlRepository.existsByShortCode(anyString())).thenReturn(false)
        val captor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(captor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val now = OffsetDateTime.now()
        val result = service.shorten("https://example.com/a", "me@example.com", now.plusDays(2), null)

        val saved = captor.value
        assertEquals(user, saved.owner)
        assertEquals(saved, result)
        verify(userRepository, times(1)).findByEmail("me@example.com")
    }

    @Test
    fun `shorten throws NOT_FOUND when owner email missing`() {
        `when`(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty())

        val ex = assertThrows<ResponseStatusException> {
            service.shorten("https://example.com", "missing@example.com", null, null)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        verify(userRepository, times(1)).findByEmail("missing@example.com")
        verifyNoMoreInteractions(userRepository)
        verify(urlRepository, never()).save(any(Url::class.java))
    }

    @Test
    fun `shorten throws BAD_REQUEST for invalid url`() {
        val ex = assertThrows<ResponseStatusException> {
            service.shorten("ftp://example.com/x", null, null, null)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        verifyNoInteractions(urlRepository, userRepository)
    }
}
