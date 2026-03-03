package org.kopytsia.urlshortener.dao.impl

import org.jooq.DSLContext
import org.kopytsia.jooq.tables.Users.Companion.USERS
import org.kopytsia.jooq.tables.records.UsersRecord
import org.kopytsia.urlshortener.dao.UserDao
import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserDaoImpl(private val dsl: DSLContext) : UserDao {

    override fun findById(id: UUID): User? = dsl
        .selectFrom(USERS)
        .where(USERS.ID.eq(id))
        .fetchOne()
        ?.toUser()

    override fun existsById(id: UUID): Boolean =
        dsl.fetchExists(USERS, USERS.ID.eq(id))

    override fun deleteById(id: UUID) {
        dsl.deleteFrom(USERS).where(USERS.ID.eq(id)).execute()
    }

    override fun save(newUser: User): User = dsl
        .insertInto(USERS)
        .set(USERS.ID, newUser.id)
        .set(USERS.EMAIL, newUser.email)
        .set(USERS.PASSWORD_HASH, newUser.passwordHash)
        .set(USERS.ROLE, newUser.role.name)
        .set(USERS.CREATED_AT, newUser.createdAt)
        .onConflict(USERS.ID)
        .doUpdate()
        .set(USERS.EMAIL, newUser.email)
        .set(USERS.PASSWORD_HASH, newUser.passwordHash)
        .set(USERS.ROLE, newUser.role.name)
        .returning()
        .fetchOne()!!
        .toUser()

    override fun findByEmail(email: String): User? = dsl
        .selectFrom(USERS)
        .where(USERS.EMAIL.eq(email))
        .fetchOne()
        ?.toUser()

    override fun existsByEmail(email: String): Boolean =
        dsl.fetchExists(USERS, USERS.EMAIL.eq(email))

    private fun UsersRecord.toUser() = User(
        id = id,
        email = email,
        passwordHash = passwordHash,
        role = Role.valueOf(role!!),
        createdAt = createdAt!!
    )
}