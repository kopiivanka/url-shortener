package org.kopytsia.urlshortener.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.repository.UserRepository
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.OffsetDateTime
import java.util.*

class UrlServiceImplTest {

    private lateinit var urlRepository: UrlRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: UrlServiceImpl

    @BeforeEach
    fun setUp() {
        urlRepository = mock(UrlRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        service = UrlServiceImpl(urlRepository, userRepository)
    }

    @Test
    fun `shorten with custom code saves entity`() {
        val custom = "myCode_1"
        `when`(urlRepository.existsByShortCode(custom)).thenReturn(false)

        val captor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(captor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val result = service.shorten(
            originalUrl = "https://example.com/path",
            ownerId = null,
            expiresAt = null,
            customCode = custom
        )

        verify(urlRepository, times(1)).existsByShortCode(custom)
        verify(urlRepository, times(1)).save(any(Url::class.java))
        verifyNoMoreInteractions(urlRepository)

        val saved = captor.value
        assertEquals(custom, saved.shortCode)
        assertEquals("https://example.com/path", saved.originalUrl)
        assertNull(saved.owner)
        assertNull(saved.expiresAt)
        assertEquals(saved, result)
    }

    @Test
    fun `shorten rejects invalid custom code`() {
        assertThrows<IllegalArgumentException> {
            service.shorten("https://example.com", null, null, "ab")
        }
        verifyNoInteractions(urlRepository)
    }

    @Test
    fun `shorten generates random code when not provided`() {
        `when`(urlRepository.existsByShortCode(anyString())).thenReturn(false)
        val captor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(captor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val result = service.shorten("https://example.com/x", null, null, null)

        verify(urlRepository, atLeastOnce()).existsByShortCode(anyString())
        verify(urlRepository, times(1)).save(any(Url::class.java))
        verifyNoMoreInteractions(urlRepository)

        val saved = captor.value
        assertEquals(saved, result)
        assertEquals("https://example.com/x", saved.originalUrl)
        assertTrue(saved.shortCode.matches(Regex("^[A-Za-z0-9]{8,12}$")))
    }

    @Test
    fun `shorten associates owner when provided`() {
        val ownerId = UUID.randomUUID()
        val user = User(id = ownerId, email = "u@example.com", passwordHash = "hash", role = Role.USER)
        `when`(userRepository.findById(ownerId)).thenReturn(Optional.of(user))
        `when`(urlRepository.existsByShortCode(anyString())).thenReturn(false)
        val captor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(captor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val result = service.shorten("https://ex.com", ownerId, null, null)

        val saved = captor.value
        assertEquals(saved, result)
        assertNotNull(saved.owner)
        assertEquals(ownerId, saved.owner?.id)
    }

    @Test
    fun `shorten throws when owner not found`() {
        val ownerId = UUID.randomUUID()
        `when`(userRepository.findById(ownerId)).thenReturn(Optional.empty())
        `when`(urlRepository.existsByShortCode(anyString())).thenReturn(false)

        assertThrows<NoSuchElementException> {
            service.shorten("https://ex.com", ownerId, null, null)
        }
    }

    @Test
    fun `resolve returns present when not expired`() {
        val url = Url(shortCode = "abc12345", originalUrl = "https://e.com", expiresAt = OffsetDateTime.now().plusDays(1))
        `when`(urlRepository.findByShortCode("abc12345")).thenReturn(Optional.of(url))

        val result = service.resolve("abc12345")
        assertTrue(result.isPresent)
        assertEquals(url, result.get())
    }

    @Test
    fun `resolve returns empty when expired`() {
        val url = Url(shortCode = "expired1", originalUrl = "https://e.com", expiresAt = OffsetDateTime.now().minusMinutes(1))
        `when`(urlRepository.findByShortCode("expired1")).thenReturn(Optional.of(url))

        val result = service.resolve("expired1")
        assertTrue(result.isEmpty)
    }

    @Test
    fun `resolve returns empty when not found`() {
        `when`(urlRepository.findByShortCode("none")).thenReturn(Optional.empty())
        val result = service.resolve("none")
        assertTrue(result.isEmpty)
    }

    @Test
    fun `shorten rejects unsupported scheme`() {
        assertThrows<IllegalArgumentException> { service.shorten("ftp://example.com", null, null, null) }
    }

    @Test
    fun `shorten rejects URL without host`() {
        assertThrows<IllegalArgumentException> { service.shorten("https://", null, null, null) }
    }

    @Test
    fun `shortenForUser looks up owner by email and delegates`() {
        val owner = User(id = UUID.randomUUID(), email = "u@example.com", passwordHash = "h", role = Role.USER)
        `when`(userRepository.findByEmail(owner.email)).thenReturn(Optional.of(owner))
        `when`(userRepository.findById(owner.id!!)).thenReturn(Optional.of(owner))
        `when`(urlRepository.existsByShortCode(anyString())).thenReturn(false)
        val savedCaptor = ArgumentCaptor.forClass(Url::class.java)
        `when`(urlRepository.save(savedCaptor.capture())).thenAnswer { it.getArgument<Url>(0) }

        val result = service.shortenForUser(
            ownerEmail = owner.email,
            originalUrl = "https://example.com/me",
            expiresAt = null,
            customCode = null
        )

        val saved = savedCaptor.value
        assertEquals(result, saved)
        assertNotNull(saved.owner)
        assertEquals(owner.id, saved.owner?.id)
        assertEquals("https://example.com/me", saved.originalUrl)
    }

    @Test
    fun `shortenForUser throws when email not found`() {
        `when`(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.shortenForUser("missing@example.com", "https://e.com", null, null)
        }
    }
}