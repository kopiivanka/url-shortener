package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "urls")
class Url(
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    val id: UUID? = null,

    @Column(name = "short_code", nullable = false, unique = true, length = 64)
    val shortCode: String,

    @Column(name = "original_url", nullable = false, columnDefinition = "text")
    val originalUrl: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    val owner: User? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "expires_at")
    val expiresAt: OffsetDateTime? = null
)