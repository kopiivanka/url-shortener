package org.kopytsia.urlshortener.service.impl

import jakarta.servlet.http.HttpServletRequest
import org.kopytsia.urlshortener.entity.ClickEvent
import org.kopytsia.urlshortener.repository.ClickEventRepository
import org.kopytsia.urlshortener.service.ClickEventService
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class ClickEventServiceImpl(
    private val repository: ClickEventRepository
) : ClickEventService {

    override fun trackRedirectUrl(
        code: String,
        originalUrl: String,
        incoming: MultiValueMap<String, String>,
        request: HttpServletRequest
    ): String {
        val merged = LinkedMultiValueMap<String, String>().apply {
            UriComponentsBuilder.fromUriString(originalUrl).build().queryParams.let(::putAll)
            putAll(incoming)
        }

        repository.save(
            ClickEvent(
                code = code,
                ip = request.remoteAddr,
                userAgent = request.getHeader("User-Agent"),
                acceptLanguage = request.getHeader("Accept-Language"),
                method = request.method,
                path = request.requestURI,
                createdAt = OffsetDateTime.now(ZoneOffset.UTC)
            )
        )

        return UriComponentsBuilder.fromUriString(originalUrl)
            .replaceQuery(null)
            .apply { merged.forEach { (k, v) -> v.forEach { queryParam(k, it) } } }
            .build(true)
            .toUriString()
    }
}