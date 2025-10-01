package org.kopytsia.urlshortener.service

import org.kopytsia.urlshortener.service.impl.ClickEventServiceImpl

interface ClickEventService {
    fun ingest(events: List<ClickEventServiceImpl.EventPayload>): Int
}