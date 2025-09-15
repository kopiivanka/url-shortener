package org.kopytsia.urlshortener.service

import org.kopytsia.urlshortener.entity.Url
import java.util.*

interface RedirectService {
    fun redirect(shortCode: String): Optional<Url>
}