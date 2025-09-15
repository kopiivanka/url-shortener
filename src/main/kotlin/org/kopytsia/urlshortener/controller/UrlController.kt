package org.kopytsia.urlshortener.controller

import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.dto.response.UrlResponse
import org.kopytsia.urlshortener.service.UrlShortenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/url")
class UrlController(
    private val urlShortenService: UrlShortenService,
) {

    @PostMapping("/shorten")
    fun shorten(
        principal: Principal,
        @RequestBody request: UrlShortenRequest
    ): ResponseEntity<UrlResponse> {
        val url = urlShortenService.shorten(
            originalUrl = request.originalUrl,
            ownerEmail = principal.name,
            expiresAt = request.expiresAt,
            customCode = request.customCode
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(UrlResponse.fromEntity(url))
    }
}