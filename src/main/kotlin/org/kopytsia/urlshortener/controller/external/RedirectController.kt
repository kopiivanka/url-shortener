package org.kopytsia.urlshortener.controller.external

import org.kopytsia.urlshortener.service.UrlService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RedirectController(
    private val urlService: UrlService
) {

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