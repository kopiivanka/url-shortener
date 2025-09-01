package org.kopytsia.urlshortener.repository

import org.kopytsia.urlshortener.entity.Url
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UrlRepository : JpaRepository<Url, UUID> {
    fun findByShortCode(shortCode: String): Optional<Url>
    fun existsByShortCode(shortCode: String): Boolean
}
