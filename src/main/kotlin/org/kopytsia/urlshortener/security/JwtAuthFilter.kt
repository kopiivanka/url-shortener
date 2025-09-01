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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private const val TOKEN_PREFIX = "Bearer "

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: UserDetailsService,
    private val tokenBlacklistService: TokenBlacklistService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // Skip auth endpoints entirely
        val path = request.servletPath
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response)
            return
        }
        request
            .getHeader(AUTHORIZATION)
            ?.removePrefix(TOKEN_PREFIX)
            ?.let { token ->
                if (jwtTokenService.isTokenValid(token) && !tokenBlacklistService.isBlacklisted(token)) {
                    doAuthorization(request, token)
                }
            }

        filterChain.doFilter(request, response)
    }

    private fun doAuthorization(
        request: HttpServletRequest,
        token: String
    ){
        val username = jwtTokenService.extractUsername(token)
        val userDetails = userDetailsService.loadUserByUsername(username)
        val authToken = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities)
            .apply {
                details= WebAuthenticationDetailsSource()
                    .buildDetails(request)
            }
        SecurityContextHolder.getContext().authentication = authToken
    }
}