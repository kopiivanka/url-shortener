package org.kopytsia.urlshortener.service.impl

import jakarta.persistence.EntityNotFoundException
import org.kopytsia.urlshortener.dao.UserDao
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.UserService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class UserServiceImpl(
    private val userDao: UserDao
) : UserService {

    @Cacheable(cacheNames = ["userById"], key = "#id")
    override fun findByUserId(id: UUID): User =
        userDao.findById(id) ?: throw EntityNotFoundException("User with id=$id not found")

    @Transactional
    @CacheEvict(cacheNames = ["userById", "userByEmail"], allEntries = true)
    override fun createUser(email: String, passwordHash: String, role: Role): User {
        val user = User(
            id = UUID.randomUUID(),
            email = email,
            passwordHash = passwordHash,
            role = role,
            createdAt = OffsetDateTime.now()
        )
        return userDao.save(user)
    }

    @Transactional
    @CacheEvict(cacheNames = ["userById", "userByEmail"], allEntries = true)
    override fun updateUser(id: UUID, email: String, passwordHash: String, role: Role): User {
        val existing = userDao.findById(id) ?: throw EntityNotFoundException("User with id=$id not found")
        val updated = User(
            id = existing.id,
            email = email,
            passwordHash = passwordHash,
            role = role,
            createdAt = existing.createdAt
        )
        return userDao.save(updated)
    }

    @Transactional
    @CacheEvict(cacheNames = ["userById", "userByEmail"], allEntries = true)
    override fun deleteUser(id: UUID) {
        if (!userDao.existsById(id)) throw EntityNotFoundException("User with id=$id not found")
        userDao.deleteById(id)
    }
}