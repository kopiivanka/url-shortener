package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.RandomCodeService
import org.kopytsia.urlshortener.service.UrlShortenService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class UrlShortenServiceImpl(
    private val urlRepository: UrlRepository,
    private val randomCodeService: RandomCodeService,
) : UrlShortenService {

    private val urlPattern = Regex("^https?://.+")
    private val codePattern = Regex("^[A-Za-z0-9_-]{3,64}$")
    private val redirectPrefix = "/r/"

    override fun shorten(
        url: String,
        user: User,
        expiresAt: OffsetDateTime?,
        code: String?
    ): String {
        if (!urlPattern.matches(url)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL format")
        }

        urlRepository.save(
            Url(
                shortCode = resolveShortCode(code),
                originalUrl = url,
                owner = user,
                expiresAt = expiresAt
            )
        )
        val shortCode = resolveShortCode(code)

        return "$redirectPrefix$shortCode"
    }

    private fun resolveShortCode(code: String?): String =
        if (!code.isNullOrBlank()) validateCustomCode(code) else generateCode()

    private fun validateCustomCode(code: String): String {
        if (!codePattern.matches(code)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid short code format")
        }
        if (urlRepository.existsByShortCode(code)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Short code already taken")
        }
        return code
    }

    private fun generateCode(): String = randomCodeService.generate()
}