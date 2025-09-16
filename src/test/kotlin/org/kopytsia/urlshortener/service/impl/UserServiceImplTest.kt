package org.kopytsia.urlshortener.service.impl

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.OffsetDateTime
import java.util.*

class UserServiceImplTest {

    private val repo = mock(UserRepository::class.java)
    private val svc = UserServiceImpl(repo)

    private fun uid() = UUID.randomUUID()
    private fun user(
        id: UUID = uid(),
        email: String = "u@example.com",
        hash: String = "h",
        role: Role = Role.USER,
        createdAt: OffsetDateTime = OffsetDateTime.now()
    ) = User(id = id, email = email, passwordHash = hash, role = role, createdAt = createdAt)

    @Test
    fun `findByUserId returns entity`() {
        val id = uid(); val u = user(id = id)
        `when`(repo.findById(id)).thenReturn(Optional.of(u))

        val got = svc.findByUserId(id)

        assertEquals(u, got)
        verify(repo).findById(id)
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `findByUserId throws when missing`() {
        val id = uid()
        `when`(repo.findById(id)).thenReturn(Optional.empty())

        assertThrows(EntityNotFoundException::class.java) { svc.findByUserId(id) }
        verify(repo).findById(id)
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `createUser saves with fields set`() {
        val cap = ArgumentCaptor.forClass(User::class.java)
        `when`(repo.save(cap.capture())).thenAnswer { it.arguments[0] }

        val res = svc.createUser("a@example.com", "hash", Role.ADMIN)

        val saved = cap.value
        assertNotNull(saved.id)
        assertEquals("a@example.com", saved.email)
        assertEquals("hash", saved.passwordHash)
        assertEquals(Role.ADMIN, saved.role)
        assertNotNull(saved.createdAt)
        assertEquals(saved, res)
        verify(repo).save(any(User::class.java))
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `updateUser updates fields, keeps id and createdAt`() {
        val id = uid()
        val created = OffsetDateTime.now().minusDays(2)
        val existing = user(id = id, email = "old@ex.com", hash = "old", role = Role.USER, createdAt = created)
        `when`(repo.findById(id)).thenReturn(Optional.of(existing))

        val cap = ArgumentCaptor.forClass(User::class.java)
        `when`(repo.save(cap.capture())).thenAnswer { it.arguments[0] }

        val res = svc.updateUser(id, "new@ex.com", "new", Role.ADMIN)

        val saved = cap.value
        assertEquals(id, saved.id)
        assertEquals(created, saved.createdAt)
        assertEquals("new@ex.com", saved.email)
        assertEquals("new", saved.passwordHash)
        assertEquals(Role.ADMIN, saved.role)
        assertEquals(saved, res)
        verify(repo).findById(id)
        verify(repo).save(any(User::class.java))
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `updateUser throws when missing`() {
        val id = uid()
        `when`(repo.findById(id)).thenReturn(Optional.empty())

        assertThrows(EntityNotFoundException::class.java) {
            svc.updateUser(id, "e@ex.com", "h", Role.USER)
        }
        verify(repo).findById(id)
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `deleteUser removes when exists`() {
        val id = uid()
        `when`(repo.existsById(id)).thenReturn(true)

        svc.deleteUser(id)

        verify(repo).existsById(id)
        verify(repo).deleteById(id)
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `deleteUser throws when missing`() {
        val id = uid()
        `when`(repo.existsById(id)).thenReturn(false)

        assertThrows(EntityNotFoundException::class.java) { svc.deleteUser(id) }
        verify(repo).existsById(id)
        verify(repo, never()).deleteById(any(UUID::class.java))
        verifyNoMoreInteractions(repo)
    }
}