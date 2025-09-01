package org.kopytsia.urlshortener.repository

import org.kopytsia.urlshortener.entity.User
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    @Cacheable(cacheNames = ["userByEmail"], key = "#email")
    fun findByEmail(email: String): Optional<User>
}