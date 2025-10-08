package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "click_events")
data class ClickEvent(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    val code: String,
    val ip: String?,
    val userAgent: String?,
    val acceptLanguage: String?,
    val method: String,
    val path: String,

    @Column(columnDefinition = "timestamptz", nullable = false)
    val createdAt: OffsetDateTime
)