package org.kopytsia.urlshortener.service.impl

import jakarta.transaction.Transactional
import org.kopytsia.urlshortener.dto.request.AuthRequest
import org.kopytsia.urlshortener.dto.request.RegisterRequest
import org.kopytsia.urlshortener.dto.response.AuthResponse
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.repository.UserRepository
import org.kopytsia.urlshortener.service.AuthService
import org.kopytsia.urlshortener.service.JwtTokenService
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService
) : AuthService {

    private fun normalizeEmail(raw: String) = raw.trim().lowercase()

    @Transactional
    override fun register(request: RegisterRequest): AuthResponse {
        val email = normalizeEmail(request.email)

        if (userRepository.findByEmail(email).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already in use")
        }

        val hash = passwordEncoder.encode(request.password)
        val user = User(email = email, passwordHash = hash)
        userRepository.save(user)

        val token = jwtTokenService.generateToken(user.email)
        return AuthResponse(token)
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    override fun login(request: AuthRequest): AuthResponse {
        val email = normalizeEmail(request.email)

        val user = userRepository.findByEmail(email).orElseThrow {
            ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }

        val matches = passwordEncoder.matches(request.password, user.passwordHash)
        if (!matches) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }

        val token = jwtTokenService.generateToken(user.email)
        return AuthResponse(token)
    }
}