package org.kopytsia.urlshortener.repository

import org.kopytsia.urlshortener.entity.ClickEvent
import org.springframework.data.jpa.repository.JpaRepository

interface ClickEventRepository :
    JpaRepository<ClickEvent, Long> {
    fun countByLinkId(linkId: String): Long }