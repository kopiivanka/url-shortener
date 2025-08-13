package org.kopytsia.urlshortener.entity

import java.io.Serializable
import java.time.LocalDate

class RollupDailyId(
    var link: String? = null,
    var day: LocalDate? = null
) : Serializable