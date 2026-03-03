package org.kopytsia.urlshortener.dao

import org.kopytsia.urlshortener.entity.User
import java.util.*

interface UserDao {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun save(user: User): User
    fun existsById(id: UUID): Boolean
    fun deleteById(id: UUID)
}