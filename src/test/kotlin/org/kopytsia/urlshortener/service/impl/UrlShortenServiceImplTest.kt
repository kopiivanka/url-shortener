package org.kopytsia.urlshortener.service.impl

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.RandomCodeService
import org.kopytsia.urlshortener.constants.TestConstants.Codes
import org.kopytsia.urlshortener.constants.TestConstants.Urls
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@ExtendWith(MockKExtension::class)
class UrlShortenServiceImplTest {

    @MockK
    lateinit var urlRepository: UrlRepository
    @MockK
    lateinit var randomCodeService: RandomCodeService
    @InjectMockKs
    lateinit var urlShortenService: UrlShortenServiceImpl

    @Test
    fun `test shorten code provided`() {
        every { urlRepository.existsByShortCode(Codes.VALID_CUSTOM) } returns false
        every { urlRepository.save(any<Url>()) } answers {
            val expected = firstArg<Url>()
            assertThat(expected.originalUrl).isEqualTo(Urls.VALID)
            assertThat(expected.shortCode).isEqualTo(Codes.VALID_CUSTOM)
            assertThat(expected.owner).isEqualTo(USER)
            assertThat(expected.expiresAt).isNull()
            expected
        }

        val actual = urlShortenService.shorten(Urls.VALID, USER, null, Codes.VALID_CUSTOM)
        assertThat(actual).isEqualTo("/r/${Codes.VALID_CUSTOM}")
        verify { urlRepository.existsByShortCode(Codes.VALID_CUSTOM) }
    }


    @Test
    fun `test shorten no code provided`() {
        every { randomCodeService.generate() } returns Codes.GENERATED
        every { urlRepository.save(any<Url>()) } answers {
            val expected = firstArg<Url>()
            assertThat(expected.originalUrl).isEqualTo(Urls.VALID)
            assertThat(expected.shortCode).isEqualTo(Codes.GENERATED)
            assertThat(expected.owner).isEqualTo(USER)
            assertThat(expected.expiresAt).isNull()
            expected
        }

        val actual = urlShortenService.shorten(Urls.VALID, USER, null, null)
        assertThat(actual).isEqualTo("/r/${Codes.GENERATED}")
        verify { randomCodeService.generate() }
    }

    @Test
    fun `test shorten invalid url provided`() {
        val actual = catchThrowableOfType(
            { urlShortenService.shorten(Urls.INVALID, USER, null, "abc") },
            ResponseStatusException::class.java
        )
        assertThat(actual.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `test shorten code invalid`() {
        val actual = catchThrowableOfType(
            { urlShortenService.shorten(Urls.VALID, USER, null, Codes.INVALID_FORMAT) },
            ResponseStatusException::class.java
        )
        assertThat(actual.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `test shorten code taken`() {
        every { urlRepository.existsByShortCode(Codes.DUPLICATE) } returns true

        val actual = catchThrowableOfType(
            { urlShortenService.shorten(Urls.VALID, USER, null, Codes.DUPLICATE) },
            ResponseStatusException::class.java
        )
        assertThat(actual.statusCode).isEqualTo(HttpStatus.CONFLICT)
        verify { urlRepository.existsByShortCode(Codes.DUPLICATE) }
    }
}