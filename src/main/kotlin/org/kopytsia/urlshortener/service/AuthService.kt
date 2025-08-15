package org.kopytsia.urlshortener.service

import org.kopytsia.urlshortener.dto.request.AuthRequest
import org.kopytsia.urlshortener.dto.request.RegisterRequest
import org.kopytsia.urlshortener.dto.response.AuthResponse

interface AuthService {
    fun register(request: RegisterRequest): AuthResponse
    fun login(request: AuthRequest): AuthResponse
}
