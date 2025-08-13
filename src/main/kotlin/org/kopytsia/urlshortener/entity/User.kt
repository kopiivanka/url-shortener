package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "users")
class User {
    @Id
    @Column(nullable = false)
    var id: UUID = UUID.randomUUID()

    @Column(nullable = false, unique = true, length = 320)
    lateinit var email: String

    @Column(name = "password_hash", nullable = false)
    lateinit var passwordHash: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var role: Role = Role.USER

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
}