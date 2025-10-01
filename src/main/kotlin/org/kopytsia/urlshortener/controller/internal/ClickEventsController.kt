package org.kopytsia.urlshortener.controller.internal

import org.kopytsia.urlshortener.service.ClickEventService
import org.kopytsia.urlshortener.service.impl.ClickEventServiceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/click-events")
class ClickEventsController(
    private val clickEventService: ClickEventService
) {
    @PostMapping
    fun ingest(@RequestBody body: List<ClickEventServiceImpl.EventPayload>): ResponseEntity<Void> =
        clickEventService.ingest(body).let {ResponseEntity.accepted().build() }
}