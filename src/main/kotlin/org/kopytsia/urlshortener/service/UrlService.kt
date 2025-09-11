package org.kopytsia.urlshortener.service

import org.kopytsia.urlshortener.entity.Url
import java.time.OffsetDateTime
import java.util.*

interface UrlService {

    fun shorten(
        originalUrl: String,
        ownerId: UUID? = null,
        expiresAt: OffsetDateTime? = null,
        customCode: String? = null
    ): Url

    fun resolve(shortCode: String): Optional<Url>

    fun shortenForUser(
        ownerEmail: String,
        originalUrl: String,
        expiresAt: OffsetDateTime? = null,
        customCode: String? = null
    ): Url
}