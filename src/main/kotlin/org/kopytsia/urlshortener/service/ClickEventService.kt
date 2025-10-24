package org.kopytsia.urlshortener.service

import jakarta.servlet.http.HttpServletRequest

interface ClickEventService {
    fun logAsync(
        code: String,
        request: HttpServletRequest
    )
}