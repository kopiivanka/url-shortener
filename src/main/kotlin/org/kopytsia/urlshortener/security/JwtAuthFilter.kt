package org.kopytsia.urlshortener.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private const val TOKEN_PREFIX = "Bearer "

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: UserDetailsService,
    private val tokenBlacklistService: TokenBlacklistService,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI ?: ""
        return path.startsWith("/api/auth/") || path.startsWith("/r/")
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(AUTHORIZATION)
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            val token = header.substring(TOKEN_PREFIX.length)
            val valid = jwtTokenService.isTokenValid(token)
            val blacklisted = tokenBlacklistService.isBlacklisted(token)

            if (valid && !blacklisted) {
                authorize(request, token)
            }
        }
        chain.doFilter(request, response)
    }

    private fun authorize(request: HttpServletRequest, token: String) {
        val username = jwtTokenService.extractUsername(token)
        val userDetails = userDetailsService.loadUserByUsername(username)
        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
    }
}