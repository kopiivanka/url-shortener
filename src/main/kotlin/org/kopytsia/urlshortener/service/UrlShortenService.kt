package org.kopytsia.urlshortener.service

import org.kopytsia.urlshortener.entity.Url
import java.time.OffsetDateTime

interface UrlShortenService {
    fun shorten(
        originalUrl: String,
        ownerEmail: String? = null,
        expiresAt: OffsetDateTime? = null,
        customCode: String? = null
    ): Url
}