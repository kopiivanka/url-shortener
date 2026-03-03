package org.kopytsia.urlshortener.dao.impl

import org.jooq.DSLContext
import org.kopytsia.jooq.tables.records.UrlsRecord
import org.kopytsia.jooq.tables.references.URLS
import org.kopytsia.urlshortener.dao.UrlDao
import org.kopytsia.urlshortener.entity.ShortUrl
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UrlDaoImpl(
    private val dsl: DSLContext
) : UrlDao {

    override fun existsByShortCode(shortCode: String): Boolean =
        dsl.fetchExists(
            dsl.selectOne()
                .from(URLS)
                .where(URLS.SHORT_CODE.eq(shortCode))
        )

    override fun findByShortCode(shortCode: String): ShortUrl? =
        dsl.selectFrom(URLS)
            .where(URLS.SHORT_CODE.eq(shortCode))
            .fetchOne()
            ?.toDomain()


    override fun save(url: ShortUrl): ShortUrl {
        val record = dsl.insertInto(URLS)
            .set(URLS.ID, url.id ?: UUID.randomUUID())
            .set(URLS.SHORT_CODE, url.shortCode)
            .set(URLS.ORIGINAL_URL, url.originalUrl)
            .set(URLS.OWNER_ID, url.ownerId)
            .set(URLS.CREATED_AT, url.createdAt)
            .set(URLS.EXPIRES_AT, url.expiresAt)
            .returning()
            .fetchOne()!!

        return record.toDomain()
    }

    private fun UrlsRecord.toDomain(): ShortUrl =
        ShortUrl(
            id = this.id,
            shortCode = this.shortCode,
            originalUrl = this.originalUrl,
            ownerId = this.ownerId,
            createdAt = this.createdAt!!,
            expiresAt = this.expiresAt
        )
}
