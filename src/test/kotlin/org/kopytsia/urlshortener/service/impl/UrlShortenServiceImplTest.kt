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

    private fun verifySavedUrl(expectedCode: String) {
        verify {
            urlRepository.save(withArg { u ->
                assertThat(u.shortCode).isEqualTo(expectedCode)
                assertThat(u.originalUrl).isEqualTo(Urls.VALID)
                assertThat(u.owner).isEqualTo(USER)
                assertThat(u.expiresAt).isNull()
            })
        }
    }

    @Test
    fun test_shorten_returnsRedirect201_whenCustomCodeProvided() {
        every { urlRepository.existsByShortCode(Codes.VALID_CUSTOM) } returns false
        every { urlRepository.save(ofType<Url>()) } returnsArgument 0

        val result = urlShortenService.shorten(Urls.VALID, USER, null, Codes.VALID_CUSTOM)
        assertThat(result).isEqualTo("/r/${Codes.VALID_CUSTOM}")
        verify { urlRepository.existsByShortCode(Codes.VALID_CUSTOM) }
        verifySavedUrl(Codes.VALID_CUSTOM)
    }

    @Test
    fun test_shorten_generatesCodeAndSaves_whenNoCustomCodeProvided() {
        every { randomCodeService.generate() } returns Codes.GENERATED
        every { urlRepository.save(ofType<Url>()) } returnsArgument 0

        val result = urlShortenService.shorten(Urls.VALID, USER, null, null)
        assertThat(result).isEqualTo("/r/${Codes.GENERATED}")
        verify { randomCodeService.generate() }
        verifySavedUrl(Codes.GENERATED)
    }

    @Test
    fun test_shorten_throws400_whenUrlIsInvalid() {
        val ex = catchThrowableOfType(
            { urlShortenService.shorten(Urls.INVALID, USER, null, "abc") },
            ResponseStatusException::class.java
        )
        assertThat(ex.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun test_shorten_throws400_whenCustomCodeFormatIsInvalid() {
        val ex = catchThrowableOfType(
            { urlShortenService.shorten(Urls.VALID, USER, null, Codes.INVALID_FORMAT) },
            ResponseStatusException::class.java
        )
        assertThat(ex.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun test_shorten_throws409_whenCustomCodeIsTaken() {
        every { urlRepository.existsByShortCode(Codes.DUPLICATE) } returns true

        val ex = catchThrowableOfType(
            { urlShortenService.shorten(Urls.VALID, USER, null, Codes.DUPLICATE) },
            ResponseStatusException::class.java
        )
        assertThat(ex.statusCode).isEqualTo(HttpStatus.CONFLICT)
        verify { urlRepository.existsByShortCode(Codes.DUPLICATE) }
    }
}