package org.kopytsia.urlshortener.controller.external

import jakarta.servlet.http.HttpServletRequest
import org.kopytsia.urlshortener.service.ClickEventService
import org.kopytsia.urlshortener.service.RedirectService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.Collections.list

@RestController
class RedirectController(
    private val redirectService: RedirectService, private val clickEventService: ClickEventService
) {

    @GetMapping("/r/{code}")
    fun getRedirectUrl(@PathVariable code: String, request: HttpServletRequest): ResponseEntity<Void> {
        val url = redirectService.getRedirectUrl(code).orElse(null)
            ?: return ResponseEntity.notFound().build()

        clickEventService.logAsync(
            code,
            request.remoteAddr,
            request.getHeader("User-Agent"),
            request.getHeader("Accept-Language"),
            request.method,
            request.requestURI,
            list(request.headerNames).associateWith { request.getHeader(it) })

        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build()
    }
}