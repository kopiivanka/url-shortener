package org.kopytsia.urlshortener.entity

import java.time.OffsetDateTime
import java.util.UUID

data class ShortUrl(
    val id: UUID? = null,
    val shortCode: String,
    val originalUrl: String,
    val ownerId: UUID?,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val expiresAt: OffsetDateTime? = null
)


