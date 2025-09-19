package org.kopytsia.urlshortener.service.impl

import io.mockk.*
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.constants.TestConstants.Ids.USER_ID
import org.kopytsia.urlshortener.constants.TestConstants.Users.PASSWORD_HASH
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER_EMAIL
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import java.time.OffsetDateTime
import java.util.*

class UserServiceImplTest {

    private val userRepository: UserRepository = mockk()
    private val userServiceImpl = UserServiceImpl(userRepository)

    @Test
    fun `test findByUserId returns entity`() {
        every { userRepository.findById(USER_ID) } returns Optional.of(USER)

        val got = userServiceImpl.findByUserId(USER_ID)

        assertEquals(USER, got)
        verify { userRepository.findById(USER_ID) }
        confirmVerified(userRepository)
    }

    @Test
    fun `test findByUserId throws when missing`() {
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) { userServiceImpl.findByUserId(id) }
        verify { userRepository.findById(id) }
        confirmVerified(userRepository)
    }

    @Test
    fun `test createUser saves with fields set`() {
        val savedSlot = slot<User>()
        every { userRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val res = userServiceImpl.createUser(USER_EMAIL, PASSWORD_HASH, Role.ADMIN)

        val saved = savedSlot.captured
        assertNotNull(saved.id)
        assertEquals(USER_EMAIL, saved.email)
        assertEquals(PASSWORD_HASH, saved.passwordHash)
        assertEquals(Role.ADMIN, saved.role)
        assertNotNull(saved.createdAt)
        assertEquals(saved, res)

        verify { userRepository.save(any<User>()) }
        confirmVerified(userRepository)
    }

    @Test
    fun `test updateUser updates fields, keeps id and createdAt`() {
        val id = UUID.randomUUID()
        val created = OffsetDateTime.now().minusDays(2)
        val existing = User(
            id = id,
            email = "old@ex.com",
            passwordHash = "old",
            role = Role.USER,
            createdAt = created
        )

        every { userRepository.findById(id) } returns Optional.of(existing)

        val savedSlot = slot<User>()
        every { userRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val res = userServiceImpl.updateUser(id, USER_EMAIL, PASSWORD_HASH, Role.ADMIN)

        val saved = savedSlot.captured
        assertEquals(id, saved.id)
        assertEquals(created, saved.createdAt)
        assertEquals(USER_EMAIL, saved.email)
        assertEquals(PASSWORD_HASH, saved.passwordHash)
        assertEquals(Role.ADMIN, saved.role)
        assertEquals(saved, res)

        verify {
            userRepository.findById(id)
            userRepository.save(any<User>())
        }
        confirmVerified(userRepository)
    }

    @Test
    fun `test updateUser throws when missing`() {
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) {
            userServiceImpl.updateUser(id, USER_EMAIL, PASSWORD_HASH, Role.USER)
        }

        verify { userRepository.findById(id) }
        confirmVerified(userRepository)
    }

    @Test
    fun `test deleteUser removes when exists`() {
        val id = UUID.randomUUID()
        every { userRepository.existsById(id) } returns true
        justRun { userRepository.deleteById(id) }

        userServiceImpl.deleteUser(id)

        verify {
            userRepository.existsById(id)
            userRepository.deleteById(id)
        }
        confirmVerified(userRepository)
    }

    @Test
    fun `test deleteUser throws when missing`() {
        val id = UUID.randomUUID()
        every { userRepository.existsById(id) } returns false

        assertThrows(EntityNotFoundException::class.java) { userServiceImpl.deleteUser(id) }

        verify { userRepository.existsById(id) }
        verify(exactly = 0) { userRepository.deleteById(any()) }
        confirmVerified(userRepository)
    }
}