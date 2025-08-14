package org.kopytsia.urlshortener.service

import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import java.util.*

interface UserService {
    fun findByUserId(id: UUID): User?
    fun createUser(email: String, passwordHash: String, role: Role): User
    fun updateUser(id: UUID, email: String?, passwordHash: String?, role: Role?): User
    fun deleteUser(id: UUID)
}