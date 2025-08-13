package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "links")
class Link {
    @Id
    @Column(length = 10, nullable = false)
    lateinit var id: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    lateinit var owner: User

    @Column(name = "long_url", nullable = false, columnDefinition = "text")
    lateinit var longUrl: String

    @Column(name = "expire_at")
    var expireAt: OffsetDateTime? = null

    @Column(nullable = false)
    var active: Boolean = true

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @ElementCollection
    @CollectionTable(
        name = "link_tags",
        joinColumns = [JoinColumn(name = "link_id")]
    )
    @Column(name = "tag", length = 64)
    var tags: MutableSet<String> = linkedSetOf()
}