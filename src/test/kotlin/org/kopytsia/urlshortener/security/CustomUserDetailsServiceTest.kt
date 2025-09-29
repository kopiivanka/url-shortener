package org.kopytsia.urlshortener.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*

class CustomUserDetailsServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val service = CustomUserDetailsService(userRepository)

    @Test
    fun `test loads existing user with correct authorities`() {
        val u = User(
            id = UUID.randomUUID(),
            email = "admin@example.com",
            passwordHash = "hash",
            role = Role.ADMIN
        )
        `when`(userRepository.findByEmail(u.email)).thenReturn(Optional.of(u))

        val details = service.loadUserByUsername(u.email)

        assertEquals(u.email, details.username)
        assertEquals(u.passwordHash, details.password)
        assertTrue(details.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `test throws when user not found`() {
        `when`(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty())
        assertThrows(UsernameNotFoundException::class.java) {
            service.loadUserByUsername("missing@example.com")
        }
    }
}