package org.kopytsia.urlshortener.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "rollups_daily")
@IdClass(RollupDailyId::class)
class RollupDaily {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    lateinit var link: Link

    @Id
    @Column(nullable = false)
    lateinit var day: LocalDate

    @Column(nullable = false)
    var clicks: Int = 0

    @Column(name = "top_referrers", columnDefinition = "text")
    var topReferrers: String = "{}"
}