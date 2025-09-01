package org.kopytsia.urlshortener.controller

import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.dto.response.UrlResponse
import org.kopytsia.urlshortener.service.UrlService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
class UrlController(
    private val urlService: UrlService,
) {

    @PostMapping("/api/url/shorten")
    fun shorten(
        principal: Principal,
        @RequestBody request: UrlShortenRequest
    ): ResponseEntity<UrlResponse> {
        val url = urlService.shortenForUser(
            ownerEmail = principal.name,
            originalUrl = request.originalUrl,
            expiresAt = request.expiresAt,
            customCode = request.customCode
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(UrlResponse.fromEntity(url))
    }

    @GetMapping("/r/{code}")
    fun redirect(@PathVariable code: String): ResponseEntity<Void> {
        val urlOpt = urlService.resolve(code)
        return if (urlOpt.isPresent) {
            ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, urlOpt.get().originalUrl)
                .build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}
