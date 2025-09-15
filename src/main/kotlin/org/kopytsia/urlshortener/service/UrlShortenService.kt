package org.kopytsia.urlshortener.service

import org.kopytsia.urlshortener.entity.User
import java.time.OffsetDateTime

interface UrlShortenService {
    fun shorten(
        url: String,
        user: User,
        expiresAt: OffsetDateTime? = null,
        code: String ? = null
    ): String
}