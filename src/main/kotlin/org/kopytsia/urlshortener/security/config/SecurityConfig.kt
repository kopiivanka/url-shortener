package org.kopytsia.urlshortener.security.config

import org.kopytsia.urlshortener.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.kopytsia.urlshortener.security.SecurityLoggingFilter
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        securityLoggingFilter: SecurityLoggingFilter
    ): SecurityFilterChain {
        val logger = LoggerFactory.getLogger(SecurityConfig::class.java)
        val defaultEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
        val defaultDenied = AccessDeniedHandlerImpl()
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/r/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { request, response, authException ->
                    logger.warn(
                        "AuthEntryPoint: method={} path={} -> 401 msg={}",
                        request.method, request.requestURI, authException.message
                    )
                    defaultEntryPoint.commence(request, response, authException)
                }
                it.accessDeniedHandler { request, response, accessDeniedException ->
                    val auth = SecurityContextHolder.getContext().authentication
                    logger.warn(
                        "AccessDenied: method={} path={} -> 403 authUser={} roles={} msg={}",
                        request.method,
                        request.requestURI,
                        auth?.name,
                        auth?.authorities,
                        accessDeniedException.message
                    )
                    defaultDenied.handle(request, response, accessDeniedException)
                }
            }
            .addFilterBefore(securityLoggingFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = false
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}