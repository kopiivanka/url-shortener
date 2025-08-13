package org.kopytsia.urlshortener.repository

import org.kopytsia.urlshortener.entity.Link
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LinkRepository :
    JpaRepository<Link, String> {
    fun findAllByOwnerId(ownerId: UUID): List<Link> }