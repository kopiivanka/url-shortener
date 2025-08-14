package org.kopytsia.urlshortener.service.impl

import jakarta.persistence.EntityNotFoundException
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.UserService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository) : UserService {

    override fun findByUserId (id: UUID): User? {
        return userRepository.findById(id)
            .orElseThrow {
                EntityNotFoundException("User with id=$id not found") }
    }

    override fun createUser
    (email: String, passwordHash: String, role: Role): User {
        val newUser = User(
            id = UUID.randomUUID(),
            email = email,
            passwordHash = passwordHash,
            role = role,
            createdAt = OffsetDateTime.now()
        )
        return userRepository.save(newUser)
    }

    override fun updateUser
    (id: UUID, email: String?, passwordHash: String?, role: Role?): User {
        val existingUser = userRepository.findById(id).orElseThrow {
            UsernameNotFoundException("User not found with id: $id") }

        val updatedUser = User(
            id = existingUser.id,
            email = email ?: existingUser.email,
            passwordHash = passwordHash ?: existingUser.passwordHash,
            role = role ?: existingUser.role,
            createdAt = existingUser.createdAt
        )
        return userRepository.save(updatedUser)
    }

    override fun deleteUser(id: UUID) {
        userRepository.deleteById(id) }
}