package org.kopytsia.urlshortener.dto.request

data class RegisterRequest(
    val email: String,
    val password: String
)