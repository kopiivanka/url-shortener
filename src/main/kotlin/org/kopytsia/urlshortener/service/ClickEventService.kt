package org.kopytsia.urlshortener.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.util.MultiValueMap

interface ClickEventService {
    fun trackRedirectUrl(
        code: String,
        originalUrl: String,
        incoming: MultiValueMap<String, String>,
        request: HttpServletRequest
    ): String
}