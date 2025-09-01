package org.kopytsia.urlshortener.service.impl

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Duration
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userService = UserServiceImpl(userRepository)
    }

    @Test
    fun `findByUserId returns user when exists`() {
        val id = UUID.randomUUID()
        val user = User(
            id = id,
            email = "john.doe@example.com",
            passwordHash = "hashed",
            role = Role.USER,
            createdAt = OffsetDateTime.now()
        )
        `when`(userRepository.findById(id)).thenReturn(Optional.of(user))

        val result = userService.findByUserId(id)

        assertEquals(user, result)
        verify(userRepository, times(1)).findById(id)
        verifyNoMoreInteractions(userRepository)
    }

    @Test
    fun `findByUserId throws when not found`() {
        val id = UUID.randomUUID()
        `when`(userRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> { userService.findByUserId(id) }

        verify(userRepository, times(1)).findById(id)
        verifyNoMoreInteractions(userRepository)
    }

    @Test
    fun `createUser saves and returns created entity`() {
        val email = "alice@example.com"
        val passwordHash = "secretHash"
        val role = Role.ADMIN

        val captor = ArgumentCaptor.forClass(User::class.java)
        `when`(userRepository.save(captor.capture())).thenAnswer { it.getArgument<User>(0) }

        val result = userService.createUser(email, passwordHash, role)
        verify(userRepository, times(1)).save(any(User::class.java))
        verifyNoMoreInteractions(userRepository)

        val saved = captor.value
        assertNotNull(saved.id, "Expected generated id")
        assertEquals(email, saved.email)
        assertEquals(passwordHash, saved.passwordHash)
        assertEquals(role, saved.role)
        assertTrue(Duration.between(saved.createdAt, OffsetDateTime.now()).seconds < 5)

        assertEquals(saved, result)
    }

    @Test
    fun `updateUser updates fields and preserves id and createdAt`() {
        val existingId = UUID.randomUUID()
        val createdAt = OffsetDateTime.now().minusDays(1)
        val existing = User(
            id = existingId,
            email = "old@example.com",
            passwordHash = "oldHash",
            role = Role.USER,
            createdAt = createdAt
        )

        `when`(userRepository.findById(existingId)).thenReturn(Optional.of(existing))

        val captor = ArgumentCaptor.forClass(User::class.java)
        `when`(userRepository.save(captor.capture())).thenAnswer { it.getArgument<User>(0) }

        val updatedEmail = "new@example.com"
        val updatedHash = "newHash"
        val updatedRole = Role.ADMIN

        val result = userService.updateUser(existingId, updatedEmail, updatedHash, updatedRole)

        verify(userRepository, times(1)).findById(existingId)
        verify(userRepository, times(1)).save(any(User::class.java))
        verifyNoMoreInteractions(userRepository)

        val saved = captor.value
        assertEquals(existingId, saved.id)
        assertEquals(createdAt, saved.createdAt)
        assertEquals(updatedEmail, saved.email)
        assertEquals(updatedHash, saved.passwordHash)
        assertEquals(updatedRole, saved.role)

        assertEquals(saved, result)
    }

    @Test
    fun `updateUser throws when user not found`() {
        val id = UUID.randomUUID()
        `when`(userRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
            userService.updateUser(id, "e@example.com", "hash", Role.USER)
        }

        verify(userRepository, times(1)).findById(id)
        verifyNoMoreInteractions(userRepository)
    }

    @Test
    fun `deleteUser deletes when exists`() {
        val id = UUID.randomUUID()
        `when`(userRepository.existsById(id)).thenReturn(true)

        userService.deleteUser(id)

        verify(userRepository, times(1)).existsById(id)
        verify(userRepository, times(1)).deleteById(id)
        verifyNoMoreInteractions(userRepository)
    }

    @Test
    fun `deleteUser throws when not found`() {
        val id = UUID.randomUUID()
        `when`(userRepository.existsById(id)).thenReturn(false)

        assertThrows<EntityNotFoundException> { userService.deleteUser(id) }

        verify(userRepository, times(1)).existsById(id)
        verify(userRepository, never()).deleteById(any(UUID::class.java))
        verifyNoMoreInteractions(userRepository)
    }
}