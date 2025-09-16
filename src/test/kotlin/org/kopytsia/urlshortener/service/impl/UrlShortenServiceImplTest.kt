package org.kopytsia.urlshortener.service.impl

import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.RandomCodeService
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@ExtendWith(MockKExtension::class)
class UrlShortenServiceImplTest {

    @MockK lateinit var repo: UrlRepository
    @MockK lateinit var rnd: RandomCodeService
    @InjectMockKs lateinit var service: UrlShortenServiceImpl

    private val user = User(UUID.randomUUID(), "me@example.com", "h", Role.USER)

    @Test
    fun `custom code 201 returns redirect`() {
        every { repo.existsByShortCode("Good_123") } returns false
        every { repo.save(any<Url>()) } answers { firstArg<Url>() }

        val r = service.shorten("https://ex.com", user, null, "Good_123")

        assertThat(r).isEqualTo("/r/Good_123")
        verify { repo.existsByShortCode("Good_123"); repo.save(any<Url>()) }
    }

    @Test
    fun `no code generate and save`() {
        every { rnd.generate() } returns "genCode"
        every { repo.save(any<Url>()) } answers { firstArg<Url>() }

        val r = service.shorten("https://ex.com", user, null, null)

        assertThat(r).isEqualTo("/r/genCode")

        verify(exactly = 1) { rnd.generate() }
        verify(exactly = 1) { repo.save(any<Url>()) }
    }

    @Test
    fun `bad url 400`() {
        val ex = catchThrowableOfType(
            { service.shorten("ftp://bad", user, null, "abc") },
            ResponseStatusException::class.java
        )
        assertThat(ex.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `bad code format 400`() {
        val ex = catchThrowableOfType(
            { service.shorten("https://ex.com", user, null, "bad space") },
            ResponseStatusException::class.java
        )
        assertThat(ex.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `code taken 409`() {
        every { repo.existsByShortCode("dup") } returns true

        val ex = catchThrowableOfType(
            { service.shorten("https://ex.com", user, null, "dup") },
            ResponseStatusException::class.java
        )
        assertThat(ex.statusCode).isEqualTo(HttpStatus.CONFLICT)
        verify { repo.existsByShortCode("dup") }
    }
}