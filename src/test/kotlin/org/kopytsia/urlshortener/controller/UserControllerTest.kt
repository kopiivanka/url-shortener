package org.kopytsia.urlshortener.controller

import io.mockk.every
import io.mockk.justRun
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.constants.TestConstants.Ids.USER_ID
import org.kopytsia.urlshortener.constants.TestConstants.Users.PASSWORD_HASH
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER_EMAIL
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.UserService
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class UserControllerTest {
    private lateinit var mockMvc: MockMvc

    @MockK
    lateinit var userService: UserService
    @MockK
    lateinit var jwtTokenService: JwtTokenService
    @MockK
    lateinit var tokenBlacklistService: TokenBlacklistService
    @MockK
    lateinit var userDetailsService: UserDetailsService

    @InjectMockKs
    lateinit var controller: UserController

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `test get ok`() {
        val id = USER_ID
        every { userService.findByUserId(id) } returns USER

        mockMvc.perform(get("/api/user/{id}", id).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun `test update ok`() {
        val id = USER_ID
        val updated = User(id = id, email = USER_EMAIL, passwordHash = PASSWORD_HASH, role = Role.ADMIN)
        every { userService.updateUser(id, updated.email, updated.passwordHash, updated.role) } returns updated

        mockMvc.perform(
            put("/api/user/{id}", id)
                .param("email", updated.email)
                .param("passwordHash", updated.passwordHash)
                .param("role", updated.role.name)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }

    @Test
    fun `test delete no content`() {
        val id = USER_ID
        justRun { userService.deleteUser(id) }

        mockMvc.perform(delete("/api/user/{id}", id))
            .andExpect(status().isNoContent)
    }
}