package org.kopytsia.urlshortener.service.impl

import jakarta.persistence.EntityNotFoundException
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.UserService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    @Cacheable(cacheNames = ["userById"], key = "#id")
    override fun findByUserId(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User with id=$id not found") }
    }

    @CacheEvict(cacheNames = ["userById", "userByEmail"], allEntries = true)
    override fun createUser(email: String, passwordHash: String, role: Role): User {
        val newUser = User(
            id = UUID.randomUUID(),
            email = email,
            passwordHash = passwordHash,
            role = role,
            createdAt = OffsetDateTime.now()
        )
        return userRepository.save(newUser)
    }

    @CacheEvict(cacheNames = ["userById", "userByEmail"], allEntries = true)
    override fun updateUser(id: UUID, email: String, passwordHash: String, role: Role): User {
        val existingUser = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User with id=$id not found") }

        val updatedUser = User(
            id = existingUser.id,
            email = email,
            passwordHash = passwordHash,
            role = role,
            createdAt = existingUser.createdAt
        )
        return userRepository.save(updatedUser)
    }

    @CacheEvict(cacheNames = ["userById", "userByEmail"], allEntries = true)
    override fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw EntityNotFoundException("User with id=$id not found")
        }
        userRepository.deleteById(id)
    }
}
