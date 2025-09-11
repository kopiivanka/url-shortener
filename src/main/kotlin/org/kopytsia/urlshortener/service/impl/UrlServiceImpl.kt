package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.UrlService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.*

@Service
class UrlServiceImpl(
    private val urlRepository: UrlRepository,
    private val userRepository: UserRepository,
) : UrlService {

    private val random = SecureRandom()
    private val alphabet = (('a'..'z') + ('A'..'Z') + ('0'..'9')).joinToString("")
    private val codeRegex = Regex("^[A-Za-z0-9_-]{3,64}$")

    @Transactional
    override fun shorten(
        originalUrl: String,
        ownerId: UUID?,
        expiresAt: OffsetDateTime?,
        customCode: String?,
    ): Url {
        val normalizedUrl = validateAndNormalizeUrl(originalUrl)

        val shortCode = if (!customCode.isNullOrBlank()) {
            require(codeRegex.matches(customCode)) { "Invalid custom code format" }
            require(!urlRepository.existsByShortCode(customCode)) { "Short code already in use" }
            customCode
        } else {
            generateUniqueCode()
        }

        val owner = ownerId?.let {
            userRepository.findById(it)
                .orElseThrow { NoSuchElementException("Owner not found: $it") }
        }

        val entity = Url(
            shortCode = shortCode,
            originalUrl = normalizedUrl,
            owner = owner,
            expiresAt = expiresAt,
        )
        return urlRepository.save(entity)
    }

    override fun resolve(shortCode: String): Optional<Url> {
        val urlOpt = urlRepository.findByShortCode(shortCode)
        val now = OffsetDateTime.now()
        return urlOpt.filter { it.expiresAt == null || it.expiresAt.isAfter(now) }
    }

    override fun shortenForUser(
        ownerEmail: String,
        originalUrl: String,
        expiresAt: OffsetDateTime?,
        customCode: String?
    ): Url {
        val owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow { NoSuchElementException("Owner not found with email: $ownerEmail") }
        return shorten(
            originalUrl = originalUrl,
            ownerId = owner.id,
            expiresAt = expiresAt,
            customCode = customCode
        )
    }

    private fun validateAndNormalizeUrl(url: String): String {
        val trimmed = url.trim()
        val uri = try {
            java.net.URI(trimmed)
        } catch (ex: Exception) {
            throw IllegalArgumentException("Invalid URL", ex)
        }
        require(uri.scheme == "http" || uri.scheme == "https") { "Only http/https URLs are allowed" }
        require(!uri.host.isNullOrBlank()) { "URL must contain a host" }
        return trimmed
    }

    private fun generateUniqueCode(length: Int = 8, maxAttempts: Int = 10): String {
        var attempt = 0
        while (attempt < maxAttempts) {
            val candidate = buildString(length) {
                repeat(length) { append(alphabet[random.nextInt(alphabet.length)]) }
            }
            if (!urlRepository.existsByShortCode(candidate)) return candidate
            attempt++
        }
        return UUID.randomUUID().toString().replace("-", "").take(12)
    }
}