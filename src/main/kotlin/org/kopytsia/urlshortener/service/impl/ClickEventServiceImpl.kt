package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.entity.ClickEvent
import org.kopytsia.urlshortener.repository.ClickEventRepository
import org.kopytsia.urlshortener.service.ClickEventService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class ClickEventServiceImpl(
    private val repo: ClickEventRepository
) : ClickEventService {

    @Async
    override fun logAsync(
        code: String,
        ip: String?,
        userAgent: String?,
        acceptLanguage: String?,
        method: String?,
        path: String?,
        headers: Map<String, String>
    ) {
        val event = ClickEvent(
            code = code,
            ip = ip,
            userAgent = userAgent,
            acceptLanguage = acceptLanguage,
            method = method,
            path = path,
            headers = headers
        )
        repo.save(event)
    }
}