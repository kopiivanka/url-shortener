package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "click_event")
data class ClickEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val code: String,

    val ip: String? = null,
    val userAgent: String? = null,
    val acceptLanguage: String? = null,
    val method: String? = null,
    val path: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    val headers: Map<String, String> = emptyMap(),

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)