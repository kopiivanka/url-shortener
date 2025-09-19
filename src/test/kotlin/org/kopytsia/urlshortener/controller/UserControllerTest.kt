package org.kopytsia.urlshortener.controller

import org.junit.jupiter.api.Test
import org.kopytsia.urlshortener.constants.TestConstants.Ids.USER_ID
import org.kopytsia.urlshortener.constants.TestConstants.Users.PASSWORD_HASH
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER
import org.kopytsia.urlshortener.constants.TestConstants.Users.USER_EMAIL
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.JwtTokenService
import org.kopytsia.urlshortener.service.TokenBlacklistService
import org.kopytsia.urlshortener.service.UserService
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [UserController::class])
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @MockBean
    lateinit var userService: UserService
    @MockBean
    lateinit var jwtTokenService: JwtTokenService
    @MockBean
    lateinit var tokenBlacklistService: TokenBlacklistService
    @MockBean
    lateinit var userDetailsService: UserDetailsService

    @Test
    fun `test get ok`() {
        val id = USER_ID
        given(userService.findByUserId(id)).willReturn(USER)

        mockMvc.perform(get("/api/user/{id}", id).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun `test update ok`() {
        val id = USER_ID
        val updated = User(id = id, email = USER_EMAIL, passwordHash = PASSWORD_HASH, role = Role.ADMIN)
        given(userService.updateUser(id, updated.email, updated.passwordHash, updated.role)).willReturn(updated)

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
        doNothing().`when`(userService).deleteUser(id)

        mockMvc.perform(delete("/api/user/{id}", id))
            .andExpect(status().isNoContent)
    }
}