package org.kopytsia.urlshortener.service.impl

import jakarta.servlet.http.HttpServletRequest
import org.kopytsia.urlshortener.entity.ClickEvent
import org.kopytsia.urlshortener.repository.ClickEventRepository
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.ClickEventService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.Collections.list

@Service
class ClickEventServiceImpl(
    private val clickEventRepository: ClickEventRepository,
    private val urlRepository: UrlRepository
) : ClickEventService {

    @Async
    override fun logAsync(code: String, request: HttpServletRequest) {
        val url = urlRepository.findByShortCode(code).orElse(null) ?: return
        clickEventRepository.save(
            ClickEvent(
                codeUuid = url.id!!,
                metadata = extractMetadata(request)
            )
        )
    }

    private fun extractMetadata(req: HttpServletRequest): Map<String, String> = buildMap {
        req.remoteAddr?.takeUnless(String::isBlank)?.let { put("ip", it) }
        req.method?.takeUnless(String::isBlank)?.let { put("method", it) }
        req.requestURI?.takeUnless(String::isBlank)?.let { put("path", it) }
        req.getHeader("User-Agent")?.takeUnless(String::isBlank)?.let { put("userAgent", it) }
        req.getHeader("Accept-Language")?.takeUnless(String::isBlank)?.let { put("acceptLanguage", it) }

        list(req.headerNames).forEach { name ->
            req.getHeader(name)?.takeUnless(String::isBlank)?.let { put("header:$name", it) }
        }
    }
}