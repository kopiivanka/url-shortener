package org.kopytsia.urlshortener.service

interface RandomCodeService {
    fun generate(): String
}