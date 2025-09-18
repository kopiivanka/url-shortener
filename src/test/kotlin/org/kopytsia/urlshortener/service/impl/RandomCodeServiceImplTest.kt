package org.kopytsia.urlshortener.service.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class RandomCodeServiceImplTest {
    private val service = RandomCodeServiceImpl()
    private val pattern: Pattern = Pattern.compile("^[a-zA-Z0-9]{8}$")

    @Test
    fun `generate returns 8-char alphanumeric`() {
        repeat(100) {
            val code = service.generate()
            assertThat(code).matches(pattern)
        }
    }

    @Test
    fun `generate values in reasonable sample`() {
        val n = 512
        val codes = (1..n).map { service.generate() }
        assertThat(codes.toSet().size).isEqualTo(n)
    }
}