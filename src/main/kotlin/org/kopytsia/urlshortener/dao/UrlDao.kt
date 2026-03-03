package org.kopytsia.urlshortener.dao

import org.kopytsia.urlshortener.entity.ShortUrl

interface UrlDao {
    fun save(url: ShortUrl): ShortUrl
    fun existsByShortCode(shortCode: String): Boolean
    fun findByShortCode(shortCode: String): ShortUrl?
}