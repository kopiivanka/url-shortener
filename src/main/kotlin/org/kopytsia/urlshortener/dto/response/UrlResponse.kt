package org.kopytsia.urlshortener.dto.response

import org.kopytsia.urlshortener.entity.Url
import java.time.OffsetDateTime
import java.util.*

data class UrlResponse(
    val id: UUID?,
    val shortCode: String,
    val originalUrl: String,
    val ownerId: UUID?,
    val createdAt: OffsetDateTime,
    val expiresAt: OffsetDateTime?
) {
    companion object {
        fun fromEntity(entity: Url): UrlResponse = UrlResponse(
            id = entity.id,
            shortCode = entity.shortCode,
            originalUrl = entity.originalUrl,
            ownerId = entity.owner?.id,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
        )
    }
}

