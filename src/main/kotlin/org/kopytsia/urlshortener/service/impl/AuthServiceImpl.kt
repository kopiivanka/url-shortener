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

    @Transactional
    override fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.findByEmail(request.email).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already in use")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)
        )
        userRepository.save(user)

        val token = jwtTokenService.generateToken(user.email)
        return AuthResponse(token)
    }


    override fun login(request: AuthRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials") }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }

        val token = jwtTokenService.generateToken(user.email)
        return AuthResponse(token)
    }
}