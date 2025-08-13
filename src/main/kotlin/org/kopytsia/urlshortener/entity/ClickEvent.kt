package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "click_events")
class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    lateinit var link: Link

    @Column(name = "ts", nullable = false)
    var timestamp: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "ip", length = 64)
    var ip: String? = null

    @Column(name = "user_agent", columnDefinition = "text")
    var userAgent: String? = null

    @Column(name = "referrer", columnDefinition = "text")
    var referrer: String? = null

    @Column(name = "country", length = 2)
    var country: String? = null
}