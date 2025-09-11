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
import org.slf4j.LoggerFactory

private const val TOKEN_PREFIX = "Bearer "

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: UserDetailsService,
    private val tokenBlacklistService: TokenBlacklistService,
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI ?: ""
        if (path.startsWith("/api/auth/")) {
            log.debug("JWT filter skipped: auth endpoint path={} method={}", path, request.method)
            return true
        }
        if (path.startsWith("/r/")) {
            log.debug("JWT filter skipped: public redirect path={} method={}", path, request.method)
            return true
        }
        if (request.method.equals("OPTIONS", ignoreCase = true)) {
            log.debug("JWT filter skipped: CORS preflight path={} method=OPTIONS", path)
            return true
        }
        return false
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        request
            .getHeader(AUTHORIZATION)
            ?.takeIf { it.startsWith(TOKEN_PREFIX) }
            ?.substring(TOKEN_PREFIX.length)
            ?.let { token ->
                val valid = jwtTokenService.isTokenValid(token)
                val blacklisted = tokenBlacklistService.isBlacklisted(token)
                if (valid && !blacklisted) {
                    doAuthorization(request, token)
                } else {
                    log.debug(
                        "JWT header present but unusable: valid={} blacklisted={} path={} method={}",
                        valid,
                        blacklisted,
                        request.requestURI,
                        request.method
                    )
                }
            }

        filterChain.doFilter(request, response)
    }

    private fun doAuthorization(
        request: HttpServletRequest,
        token: String
    ) {
        val username = jwtTokenService.extractUsername(token)
        val userDetails = userDetailsService.loadUserByUsername(username)
        val authToken = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )
            .apply {
                details = WebAuthenticationDetailsSource()
                    .buildDetails(request)
            }
        SecurityContextHolder.getContext().authentication = authToken
    }
}