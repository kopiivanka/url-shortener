package org.kopytsia.urlshortener.controller

import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.dto.response.UrlResponse
import org.kopytsia.urlshortener.service.UrlService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/url")
class UrlController(
    private val urlService: UrlService,
) {

    @PostMapping("/shorten")
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
}