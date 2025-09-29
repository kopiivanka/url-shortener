package org.kopytsia.urlshortener.dto.request

import java.time.OffsetDateTime

data class UrlShortenRequest(
    val originalUrl: String,
    val expiresAt: OffsetDateTime? = null,
    val customCode: String? = null
)