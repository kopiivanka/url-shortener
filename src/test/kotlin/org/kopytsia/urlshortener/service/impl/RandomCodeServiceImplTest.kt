package org.kopytsia.urlshortener.service.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class RandomCodeServiceImplTest {
    private val randomCodeServiceImpl = RandomCodeServiceImpl()
    private val pattern: Pattern = Pattern.compile("^[a-zA-Z0-9]{8}$")

    @Test
    fun `test generate returns 8-char alphanumeric`() {
        repeat(100) {
            val code = randomCodeServiceImpl.generate()
            assertThat(code).matches(pattern)
        }
    }

    @Test
    fun `test generate values in reasonable sample`() {
        val n = 512
        val codes = (1..n).map { randomCodeServiceImpl.generate() }
        assertThat(codes.toSet().size).isEqualTo(n)
    }
}