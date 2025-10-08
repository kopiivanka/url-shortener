package org.kopytsia.urlshortener.controller.external

import jakarta.servlet.http.HttpServletRequest
import org.kopytsia.urlshortener.service.ClickEventService
import org.kopytsia.urlshortener.service.RedirectService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

@RestController
class RedirectController(
    private val redirectService: RedirectService,
    private val clickEventService: ClickEventService
) {

    @GetMapping("/r/{code}")
    fun getRedirectUrl(
        @PathVariable code: String,
        @RequestParam params: MultiValueMap<String, String>,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val url = redirectService.getRedirectUrl(code).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val target = clickEventService.trackRedirectUrl(code, url.originalUrl, params, request)
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, target)
            .build()
    }
}