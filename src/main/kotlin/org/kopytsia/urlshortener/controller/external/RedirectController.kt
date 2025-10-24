package org.kopytsia.urlshortener.controller.external

import jakarta.servlet.http.HttpServletRequest
import org.kopytsia.urlshortener.aop.TrackRedirect
import org.kopytsia.urlshortener.service.RedirectService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class RedirectController(
    private val redirectService: RedirectService
) {
    @TrackRedirect
    @GetMapping("/r/{code}")
    fun getRedirectUrl(
        @PathVariable code: String,
        request: HttpServletRequest
    ): ResponseEntity<Void> =
        redirectService.getRedirectUrl(code)
            .map {
                ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, it)
                    .build<Void>()
            }
            .orElseGet { ResponseEntity.notFound().build() }
}