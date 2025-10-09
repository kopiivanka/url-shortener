package org.kopytsia.urlshortener.service

import java.util.*

interface RedirectService {
    fun getRedirectUrl(shortCode: String): Optional<String>
}