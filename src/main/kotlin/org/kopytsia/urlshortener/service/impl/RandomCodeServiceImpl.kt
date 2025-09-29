package org.kopytsia.urlshortener.service.impl

import org.kopytsia.urlshortener.service.RandomCodeService
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class RandomCodeServiceImpl : RandomCodeService {
    private val random = SecureRandom()
    private val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    override fun generate(): String {
        return buildString(8) {
            repeat(8) { append(alphabet[random.nextInt(alphabet.length)]) }
        }
    }
}