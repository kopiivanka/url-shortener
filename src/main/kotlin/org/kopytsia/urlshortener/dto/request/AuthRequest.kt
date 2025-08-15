package org.kopytsia.urlshortener.dto.request

data class AuthRequest(
    val email: String,
    val password: String
)