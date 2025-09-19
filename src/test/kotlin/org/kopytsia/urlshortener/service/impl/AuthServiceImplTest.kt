package org.kopytsia.urlshortener.service.impl

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.constants.TestConstants.Users.PASSWORD_HASH
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER_EMAIL
import org.kopytsia.urlshortener.dto.request.AuthRequest
import org.kopytsia.urlshortener.dto.request.RegisterRequest
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.JwtTokenService
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AuthServiceImplTest {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var jwt: JwtTokenService

    @InjectMockKs
    lateinit var svc: AuthServiceImpl

    @Test
    fun `test register creates user and returns token`() {
        val req = RegisterRequest(email = "new@example.com", password = "plain")

        every { userRepository.findByEmail(req.email) } returns Optional.empty()
        every { passwordEncoder.encode(req.password) } returns "enc"
        every { jwt.generateToken(req.email) } returns "jwt"

        val saved = slot<User>()
        every { userRepository.save(capture(saved)) } answers { saved.captured }

        val res = svc.register(req)

        assertEquals(req.email, saved.captured.email)
        assertEquals("enc", saved.captured.passwordHash)
        assertEquals(Role.USER, saved.captured.role)
        assertEquals("jwt", res.token)

        verify {
            userRepository.findByEmail(req.email)
            passwordEncoder.encode(req.password)
            userRepository.save(any())
            jwt.generateToken(req.email)
        }
        confirmVerified(userRepository, passwordEncoder, jwt)
    }

    @Test
    fun `test register throws 409 when email taken`() {
        val req = RegisterRequest(email = USER_EMAIL, password = "pw")
        every { userRepository.findByEmail(USER_EMAIL) } returns Optional.of(USER)

        val ex = assertThrows<ResponseStatusException> { svc.register(req) }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        verify { userRepository.findByEmail(USER_EMAIL) }
        confirmVerified(userRepository)
    }

    @Test
    fun `test login returns token on valid creds`() {
        val req = AuthRequest(email = USER_EMAIL, password = "pw")

        every { userRepository.findByEmail(USER_EMAIL) } returns Optional.of(USER)
        every { passwordEncoder.matches(req.password, PASSWORD_HASH) } returns true
        every { jwt.generateToken(USER_EMAIL) } returns "jwt"

        val res = svc.login(req)

        assertEquals("jwt", res.token)
        verify {
            userRepository.findByEmail(USER_EMAIL)
            passwordEncoder.matches(req.password, PASSWORD_HASH)
            jwt.generateToken(USER_EMAIL)
        }
        confirmVerified(userRepository, passwordEncoder, jwt)
    }

    @Test
    fun `test login throws 401 when user missing`() {
        val req = AuthRequest(email = "missing@example.com", password = "pw")
        every { userRepository.findByEmail(req.email) } returns Optional.empty()

        val ex = assertThrows<ResponseStatusException> { svc.login(req) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)

        verify { userRepository.findByEmail(req.email) }
        confirmVerified(userRepository)
    }

    @Test
    fun `test login throws 401 when password invalid`() {
        val req = AuthRequest(email = USER_EMAIL, password = "bad")
        every { userRepository.findByEmail(USER_EMAIL) } returns Optional.of(USER)
        every { passwordEncoder.matches(req.password, PASSWORD_HASH) } returns false

        val ex = assertThrows<ResponseStatusException> { svc.login(req) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)

        verify {
            userRepository.findByEmail(USER_EMAIL)
            passwordEncoder.matches(req.password, PASSWORD_HASH)
        }
        confirmVerified(userRepository, passwordEncoder)
    }
}