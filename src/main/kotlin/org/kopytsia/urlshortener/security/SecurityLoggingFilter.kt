package org.kopytsia.urlshortener.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SecurityLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(SecurityLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val hasAuthHeader = request.getHeader("Authorization")?.isNotBlank() == true
        val isBearer = request.getHeader("Authorization")?.startsWith("Bearer ") == true
        log.debug(
            "SecurityLog: incoming method={} path={} hasAuthHeader={} isBearerAuth={}",
            request.method,
            request.requestURI,
            hasAuthHeader,
            isBearer
        )

        filterChain.doFilter(request, response)
    }
}