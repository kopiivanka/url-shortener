package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.dao.UrlDao
import org.kopytsia.urlshortener.service.RedirectService
import org.springframework.stereotype.Service
import java.util.*

@Service
class RedirectServiceImpl(
    private val urlDao: UrlDao,
) : RedirectService {

    override fun getRedirectUrl(shortCode: String): Optional<String> {

        val url = urlDao.findByShortCode(shortCode) ?: return Optional.empty()

        return Optional.of(url.originalUrl)
    }
}