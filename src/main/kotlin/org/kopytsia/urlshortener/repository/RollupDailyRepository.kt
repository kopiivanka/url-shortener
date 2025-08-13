package org.kopytsia.urlshortener.repository

import org.kopytsia.urlshortener.entity.RollupDaily
import org.kopytsia.urlshortener.entity.RollupDailyId
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface RollupDailyRepository :
    JpaRepository<RollupDaily, RollupDailyId> {
    fun findAllByLinkIdAndDayBetween(
        linkId: String,
        from: LocalDate,
        to: LocalDate
    ): List<RollupDaily> }