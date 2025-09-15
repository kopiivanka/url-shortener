package org.kopytsia.urlshortener.controller

import org.kopytsia.urlshortener.dto.request.UrlShortenRequest
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.UrlShortenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

@RestController
@RequestMapping("/api/url")
class UrlController(
    private val urlShortenService: UrlShortenService,
    private val userRepository: UserRepository,
) {

    @PostMapping("/shorten")
    fun shorten(
        principal: Principal,
        @RequestBody request: UrlShortenRequest
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByEmail(principal.name)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED) }

        val redirect = urlShortenService.shorten(
            url = request.originalUrl,
            user = user,
            expiresAt = request.expiresAt,
            code = request.customCode
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf("code" to redirect))
    }
}