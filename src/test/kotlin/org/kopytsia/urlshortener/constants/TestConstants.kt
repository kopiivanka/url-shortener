package org.kopytsia.urlshortener.constants

import org.kopytsia.urlshortener.entity.Role
import org.kopytsia.urlshortener.entity.User
import java.util.UUID

object TestConstants {

    object Urls {
        const val VALID = "https://ex.com"
        const val INVALID = "ftp://bad"
    }

    object Codes {
        const val VALID_CUSTOM = "Good_123"
        const val GENERATED = "genCode"
        const val DUPLICATE = "dup"
        const val INVALID_FORMAT = "bad space"
    }

    object Ids {
        val USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    }

    object Users {
        const val USER_EMAIL = "me@example.com"
        const val PASSWORD_HASH = "hashed"

        val USER = User(Ids.USER_ID, USER_EMAIL, PASSWORD_HASH, Role.USER)
    }
}
