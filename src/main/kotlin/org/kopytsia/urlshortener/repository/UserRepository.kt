package org.kopytsia.urlshortener.repository

import org.kopytsia.urlshortener.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UUID>