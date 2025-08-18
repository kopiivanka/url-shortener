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

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val passwordEncoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val jwtTokenService: JwtTokenService = mock(JwtTokenService::class.java)

    private val authService = AuthServiceImpl(userRepository, passwordEncoder, jwtTokenService)

    @Test
    fun `register creates user, encodes password and returns token`() {
        val req = RegisterRequest(email = "new@example.com", password = "plainPass")
        `when`(userRepository.findByEmail(req.email)).thenReturn(Optional.empty())
        `when`(passwordEncoder.encode(req.password)).thenReturn("encodedPass")
        val captor = ArgumentCaptor.forClass(User::class.java)
        `when`(userRepository.save(captor.capture())).thenAnswer { it.getArgument<User>(0) }
        `when`(jwtTokenService.generateToken(req.email)).thenReturn("jwt-token")

        val response = authService.register(req)

        verify(userRepository, times(1)).findByEmail(req.email)
        verify(passwordEncoder, times(1)).encode(req.password)
        verify(userRepository, times(1)).save(any(User::class.java))
        verify(jwtTokenService, times(1)).generateToken(req.email)
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtTokenService)

        val saved = captor.value
        assertEquals(req.email, saved.email)
        assertEquals("encodedPass", saved.passwordHash)
        assertEquals(Role.USER, saved.role)

        assertEquals("jwt-token", response.token)
    }

    @Test
    fun `register throws CONFLICT when email already in use`() {
        val req = RegisterRequest(email = "taken@example.com", password = "pw")
        `when`(userRepository.findByEmail(req.email)).thenReturn(Optional.of(mock(User::class.java)))

        val ex = assertThrows<ResponseStatusException> { authService.register(req) }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        verify(userRepository, times(1)).findByEmail(req.email)
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(passwordEncoder, jwtTokenService)
    }

    @Test
    fun `login returns token when credentials valid`() {
        val req = AuthRequest(email = "user@example.com", password = "pw")
        val stored = User(id = UUID.randomUUID(), email = req.email, passwordHash = "hashed")
        `when`(userRepository.findByEmail(req.email)).thenReturn(Optional.of(stored))
        `when`(passwordEncoder.matches(req.password, stored.passwordHash)).thenReturn(true)
        `when`(jwtTokenService.generateToken(req.email)).thenReturn("jwt-token")

        val response = authService.login(req)

        verify(userRepository, times(1)).findByEmail(req.email)
        verify(passwordEncoder, times(1)).matches(req.password, stored.passwordHash)
        verify(jwtTokenService, times(1)).generateToken(req.email)
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtTokenService)

        assertEquals("jwt-token", response.token)
    }

    @Test
    fun `login throws UNAUTHORIZED when user not found`() {
        val req = AuthRequest(email = "missing@example.com", password = "pw")
        `when`(userRepository.findByEmail(req.email)).thenReturn(Optional.empty())

        val ex = assertThrows<ResponseStatusException> { authService.login(req) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)

        verify(userRepository, times(1)).findByEmail(req.email)
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(passwordEncoder, jwtTokenService)
    }

    @Test
    fun `login throws UNAUTHORIZED when password invalid`() {
        val req = AuthRequest(email = "user@example.com", password = "bad")
        val stored = User(id = UUID.randomUUID(), email = req.email, passwordHash = "hashed")
        `when`(userRepository.findByEmail(req.email)).thenReturn(Optional.of(stored))
        `when`(passwordEncoder.matches(req.password, stored.passwordHash)).thenReturn(false)

        val ex = assertThrows<ResponseStatusException> { authService.login(req) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)

        verify(userRepository, times(1)).findByEmail(req.email)
        verify(passwordEncoder, times(1)).matches(req.password, stored.passwordHash)
        verifyNoMoreInteractions(userRepository, passwordEncoder)
        verifyNoInteractions(jwtTokenService)
    }
}