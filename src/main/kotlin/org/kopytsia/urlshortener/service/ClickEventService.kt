package org.kopytsia.urlshortener.service

interface ClickEventService {
    fun logAsync(
        code: String,
        ip: String?,
        userAgent: String?,
        acceptLanguage: String?,
        method: String?,
        path: String?,
        headers: Map<String, String>
    )
}