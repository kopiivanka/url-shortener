package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.UrlShortenService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.*

@Service
class UrlShortenServiceImpl(
    private val urlRepository: UrlRepository,
    private val userRepository: UserRepository,
) : UrlShortenService {

    private val random = SecureRandom()
    private val alphabet = (('a'..'z') + ('A'..'Z') + ('0'..'9')).joinToString("")
    private val codeRegex = Regex("^[A-Za-z0-9_-]{3,64}$")

    @Transactional
    override fun shorten(
        originalUrl: String,
        ownerEmail: String?,
        expiresAt: OffsetDateTime?,
        customCode: String?
    ): Url {
        val normalizedUrl = validateAndNormalizeUrl(originalUrl)
        val shortCode = generateOrValidateCode(customCode)

        val owner = if (!ownerEmail.isNullOrBlank()) {
            val email = ownerEmail.trim().lowercase()
            userRepository.findByEmail(email).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        } else null

        val entity = Url(
            shortCode = shortCode,
            originalUrl = normalizedUrl,
            owner = owner,
            expiresAt = expiresAt
        )
        return urlRepository.save(entity)
    }

    private fun generateOrValidateCode(customCode: String?): String {
        if (!customCode.isNullOrBlank()) {
            if (!codeRegex.matches(customCode)) throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            if (urlRepository.existsByShortCode(customCode)) throw ResponseStatusException(HttpStatus.CONFLICT)
            return customCode
        }
        return generateUniqueCode()
    }

    private fun generateUniqueCode(length: Int = 8, maxAttempts: Int = 10): String {
        repeat(maxAttempts) {
            val candidate = buildString(length) {
                repeat(length) { append(alphabet[random.nextInt(alphabet.length)]) }
            }
            if (!urlRepository.existsByShortCode(candidate)) return candidate
        }
        return UUID.randomUUID().toString().replace("-", "").take(12)
    }

    private fun validateAndNormalizeUrl(url: String): String {
        val trimmed = url.trim()
        val uri = try { java.net.URI(trimmed) } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        if (uri.scheme != "http" && uri.scheme != "https") throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        if (uri.host.isNullOrBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        return trimmed
    }
}