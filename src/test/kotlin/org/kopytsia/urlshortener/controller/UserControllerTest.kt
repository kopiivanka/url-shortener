package org.kopytsia.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.UserService
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(controllers = [UserController::class])
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @MockBean lateinit var userService: UserService
    @MockBean lateinit var jwtTokenService: JwtTokenService
    @MockBean lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean lateinit var userDetailsService: UserDetailsService

    @Test
    fun getByUserId_returns_ok() {
        val id = UUID.randomUUID()
        val user = User(id = id, email = "e@example.com", passwordHash = "h", role = Role.USER)
        `when`(userService.findByUserId(id)).thenReturn(user)

        mockMvc.perform(
            get("/api/user/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }

    @Test
    fun updateByUserId_returns_ok() {
        val id = UUID.randomUUID()
        val updated = User(id = id, email = "new@example.com", passwordHash = "nh", role = Role.ADMIN)
        `when`(userService.updateUser(id, updated.email, updated.passwordHash, updated.role)).thenReturn(updated)

        mockMvc.perform(
            put("/api/user/{id}", id)
                .param("email", updated.email)
                .param("passwordHash", updated.passwordHash)
                .param("role", updated.role.name)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }

    @Test
    fun deleteByUserId_returns_no_content() {
        val id = UUID.randomUUID()
        Mockito.doNothing().`when`(userService).deleteUser(id)

        mockMvc.perform(
            delete("/api/user/{id}", id)
        ).andExpect(status().isNoContent)
    }
}