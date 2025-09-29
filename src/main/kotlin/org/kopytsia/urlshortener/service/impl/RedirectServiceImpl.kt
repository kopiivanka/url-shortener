package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.entity.Url
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.RedirectService
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class RedirectServiceImpl(
    private val urlRepository: UrlRepository,
) : RedirectService {

    override fun getRedirectUrl(shortCode: String): Optional<Url> {
        return urlRepository.findByShortCode(shortCode)
            .filter { it.expiresAt == null || it.expiresAt.isAfter( OffsetDateTime.now()) }
    }
}