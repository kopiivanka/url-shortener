package org.kopytsia.urlshortener.repository

import org.kopytsia.urlshortener.entity.ClickEvent
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClickEventRepository : JpaRepository<ClickEvent, UUID>