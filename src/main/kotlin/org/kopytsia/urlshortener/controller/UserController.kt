package org.kopytsia.urlshortener.controller

import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.kopytsia.urlshortener.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {

    @GetMapping("{id}")
    fun getByUserId(@PathVariable id: UUID): ResponseEntity<User> {
        val user = userService.findByUserId(id)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @PutMapping("{id}")
    fun updateByUserId(
        @PathVariable id: UUID,
        @RequestParam email: String,
        @RequestParam passwordHash: String,
        @RequestParam role: Role
    ): ResponseEntity<User> {
        val updatedUser = userService.updateUser(id, email, passwordHash, role)
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser)
    }

    @DeleteMapping("{id}")
    fun deleteByUserId(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}