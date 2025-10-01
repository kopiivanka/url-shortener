package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.entity.ClickEvent
import org.kopytsia.urlshortener.entity.RedirectStatus
import org.kopytsia.urlshortener.repository.ClickEventRepository
import org.kopytsia.urlshortener.repository.UrlRepository
import org.kopytsia.urlshortener.service.ClickEventService
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class ClickEventServiceImpl(
    private val urlRepository: UrlRepository,
    private val clickEventRepository: ClickEventRepository
) : ClickEventService {

    data class EventPayload(
        val occurredAt: OffsetDateTime? = null,
        val code: String,
        val status: String,
        val urlId: String,
        val referer: String? = null,
        val userAgent: String? = null
    )

    override fun ingest(events: List<EventPayload>): Int {
        val ids = events.mapNotNull {
            try {
                UUID.fromString(it.urlId)
            } catch (_: IllegalArgumentException) {
                null
            }
        }.distinct()

        val urlsById = if (ids.isNotEmpty())
            urlRepository.findAllById(ids).associateBy { it.id!!.toString() }
        else emptyMap()

        val entities = events.mapNotNull { p ->
            val url = urlsById[p.urlId] ?: return@mapNotNull null
            val status = try {
                RedirectStatus.valueOf(p.status.uppercase())
            } catch (_: IllegalArgumentException) {
                null
            }
                ?: return@mapNotNull null

            ClickEvent(
                occurredAt = p.occurredAt ?: OffsetDateTime.now(),
                code = p.code,
                status = status,
                url = url,
                referer = p.referer,
                userAgent = p.userAgent
            )
        }

        return clickEventRepository.saveAll(entities).size
    }
}