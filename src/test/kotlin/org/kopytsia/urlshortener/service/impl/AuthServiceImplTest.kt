package org.kopytsia.urlshortener.service.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kopytsia.urlshortener.dto.request.AuthRequest
import org.kopytsia.urlshortener.dto.request.RegisterRequest
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.JwtTokenService
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.util.*

class AuthServiceImplTest {

    private val repo: UserRepository = mock(UserRepository::class.java)
    private val encoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val jwt: JwtTokenService = mock(JwtTokenService::class.java)
    private val svc = AuthServiceImpl(repo, encoder, jwt)

    @Test
    fun `register creates user and returns token`() {
        val req = RegisterRequest("new@example.com", "plain")
        `when`(repo.findByEmail(req.email)).thenReturn(Optional.empty())
        `when`(encoder.encode(req.password)).thenReturn("enc")
        `when`(jwt.generateToken(req.email)).thenReturn("jwt")

        val cap = ArgumentCaptor.forClass(User::class.java)
        `when`(repo.save(cap.capture())).thenAnswer { it.arguments[0] }

        val res = svc.register(req)

        val saved = cap.value
        assertEquals(req.email, saved.email)
        assertEquals("enc", saved.passwordHash)
        assertEquals(Role.USER, saved.role)
        assertEquals("jwt", res.token)
    }

    @Test
    fun `register throws 409 when email taken`() {
        val req = RegisterRequest("taken@example.com", "pw")
        val existing = User(
            id = UUID.randomUUID(),
            email = req.email,
            passwordHash = "hash",
            role = Role.USER
        )
        `when`(repo.findByEmail(req.email)).thenReturn(Optional.of(existing))

        val ex = assertThrows<ResponseStatusException> { svc.register(req) }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun `login returns token on valid creds`() {
        val req = AuthRequest("u@example.com", "pw")
        val stored = User(id = UUID.randomUUID(), email = req.email, passwordHash = "hash")
        `when`(repo.findByEmail(req.email)).thenReturn(Optional.of(stored))
        `when`(encoder.matches(req.password, stored.passwordHash)).thenReturn(true)
        `when`(jwt.generateToken(req.email)).thenReturn("jwt")

        val res = svc.login(req)

        assertEquals("jwt", res.token)
    }

    @Test
    fun `login throws 401 when user missing`() {
        val req = AuthRequest("missing@example.com", "pw")
        `when`(repo.findByEmail(req.email)).thenReturn(Optional.empty())

        val ex = assertThrows<ResponseStatusException> { svc.login(req) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun `login throws 401 when password invalid`() {
        val req = AuthRequest("u@example.com", "bad")
        val stored = User(id = UUID.randomUUID(), email = req.email, passwordHash = "hash")
        `when`(repo.findByEmail(req.email)).thenReturn(Optional.of(stored))
        `when`(encoder.matches(req.password, stored.passwordHash)).thenReturn(false)

        val ex = assertThrows<ResponseStatusException> { svc.login(req) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }
}