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
        chain: FilterChain
    ) {
        val header = request.getHeader(AUTHORIZATION)

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            val token = header.removePrefix(TOKEN_PREFIX)

            if (jwtTokenService.isTokenValid(token) && !tokenBlacklistService.isBlacklisted(token)) {
                val username = jwtTokenService.extractUsername(token)
                val user = userDetailsService.loadUserByUsername(username)

                val auth = UsernamePasswordAuthenticationToken(
                    user, null, user.authorities).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        chain.doFilter(request, response)
    }
}