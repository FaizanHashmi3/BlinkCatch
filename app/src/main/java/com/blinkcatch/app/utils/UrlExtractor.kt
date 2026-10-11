package com.blinkcatch.app.utils

import java.util.regex.Pattern

object UrlExtractor {
    private val URL_REGEX = Pattern.compile(
        "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Extracts the first valid HTTP/HTTPS URL found in shared text.
     */
    fun extractUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val matcher = URL_REGEX.matcher(text)
        if (matcher.find()) {
            var url = matcher.group(0) ?: return null
            // Strip any trailing punctuation that might get appended by users
            url = url.trimEnd('.', ',', ';', '!', '?', ')')
            return url
        }
        return null
    }

    /**
     * Checks if URL belongs to supported social platforms (YouTube, Instagram, Twitter/X, TikTok, etc.)
     */
    fun getPlatformName(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("instagram.com") -> "Instagram"
            lower.contains("twitter.com") || lower.contains("x.com") -> "X (Twitter)"
            lower.contains("tiktok.com") -> "TikTok"
            lower.contains("facebook.com") || lower.contains("fb.watch") -> "Facebook"
            else -> "Web Video"
        }
    }
}
