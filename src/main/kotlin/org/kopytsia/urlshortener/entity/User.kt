package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 320)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val role: Role = Role.USER,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
