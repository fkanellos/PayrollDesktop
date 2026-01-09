package com.payroll.app.desktop.core.utils

/**
 * Utility for normalizing strings for comparison
 * Handles Greek characters, accents, case, and extra words
 *
 * 🔥 HIGH FIX: Added caching to avoid repeated string normalization
 * With 100 events × 5 comparisons × 10 replaces = 5,000 operations → Now cached!
 */
object StringNormalizer {

    // 🔥 Cache for normalized strings to avoid repeated expensive operations
    private val normalizeCache = mutableMapOf<String, String>()
    private val normalizeForMatchingCache = mutableMapOf<Pair<String, Int>, String>()

    // Cache size limit to prevent memory issues
    private const val MAX_CACHE_SIZE = 1000

    /**
     * Normalize a string for matching:
     * - Convert to lowercase
     * - Remove accents/diacritics
     * - Trim whitespace
     * - Take only first N words (for name matching)
     *
     * Examples:
     * "ΒΑΣΙΛΙΚΗ ΣΤΑΙΚΟΥΡΑ" -> "βασιλικη σταικουρα"
     * "Βασιλική Σταικούρα Μετρητά" -> "βασιλικη σταικουρα"
     * "Μαρία Παπαδοπούλου Online" -> "μαρια παπαδοπουλου"
     *
     * 🔥 Cached for performance
     */
    fun normalizeForMatching(input: String, maxWords: Int = 2): String {
        val cacheKey = Pair(input, maxWords)

        // Check cache first
        normalizeForMatchingCache[cacheKey]?.let { return it }

        // Clear cache if too large
        if (normalizeForMatchingCache.size > MAX_CACHE_SIZE) {
            normalizeForMatchingCache.clear()
        }

        // Compute and cache
        val result = input
            .trim()
            .lowercase()
            .removeAccents()
            .split("\\s+".toRegex())
            .take(maxWords)
            .joinToString(" ")

        normalizeForMatchingCache[cacheKey] = result
        return result
    }

    /**
     * Normalize text without taking only first N words
     * Useful for matching full event titles
     *
     * Examples:
     * "ΒΑΣΙΛΙΚΗ ΣΤΑΙΚΟΥΡΑ Μετρητά" -> "βασιλικη σταικουρα μετρητα"
     *
     * 🔥 Cached for performance
     */
    fun normalize(input: String): String {
        // Check cache first
        normalizeCache[input]?.let { return it }

        // Clear cache if too large
        if (normalizeCache.size > MAX_CACHE_SIZE) {
            normalizeCache.clear()
        }

        // Compute and cache
        val result = input
            .trim()
            .lowercase()
            .removeAccents()

        normalizeCache[input] = result
        return result
    }

    /**
     * Clear normalization caches (useful for testing or memory management)
     */
    fun clearCache() {
        normalizeCache.clear()
        normalizeForMatchingCache.clear()
    }

    /**
     * Remove accents and diacritics from Greek and Latin characters
     *
     * Examples:
     * "Βασιλική" -> "βασιλικη"
     * "Σταικούρα" -> "σταικουρα"
     * "María" -> "maria"
     */
    private fun String.removeAccents(): String {
        return this
            .replace("ά", "α").replace("Ά", "α")
            .replace("έ", "ε").replace("Έ", "ε")
            .replace("ή", "η").replace("Ή", "η")
            .replace("ί", "ι").replace("Ί", "ι")
            .replace("ΐ", "ι").replace("ϊ", "ι")
            .replace("ό", "ο").replace("Ό", "ο")
            .replace("ύ", "υ").replace("Ύ", "υ")
            .replace("ΰ", "υ").replace("ϋ", "υ")
            .replace("ώ", "ω").replace("Ώ", "ω")
    }

    /**
     * Check if two names match (ignoring case, accents, and extra words)
     *
     * Examples:
     * matches("ΒΑΣΙΛΙΚΗ ΣΤΑΙΚΟΥΡΑ", "Βασιλική Σταικούρα Μετρητά") -> true
     * matches("Μαρία Παπαδοπούλου", "ΜΑΡΙΑ ΠΑΠΑΔΟΠΟΥΛΟΥ Online") -> true
     * matches("Γιάννης Δημητρίου", "Μαρία Παπαδοπούλου") -> false
     */
    fun matches(name1: String, name2: String, maxWords: Int = 2): Boolean {
        val normalized1 = normalizeForMatching(name1, maxWords)
        val normalized2 = normalizeForMatching(name2, maxWords)
        return normalized1 == normalized2
    }

    /**
     * Check if a name starts with a prefix (ignoring case and accents)
     *
     * Useful for searching/filtering
     *
     * Example:
     * startsWith("Βασιλική Σταικούρα", "βασιλ") -> true
     */
    fun startsWith(name: String, prefix: String): Boolean {
        val normalizedName = normalizeForMatching(name, maxWords = Int.MAX_VALUE)
        val normalizedPrefix = prefix.trim().lowercase().removeAccents()
        return normalizedName.startsWith(normalizedPrefix)
    }

    /**
     * Extract the first N words from a name (useful for display)
     *
     * Example:
     * extractFirstWords("Βασιλική Σταικούρα Μετρητά", 2) -> "Βασιλική Σταικούρα"
     */
    fun extractFirstWords(name: String, maxWords: Int = 2): String {
        return name.trim()
            .split("\\s+".toRegex())
            .take(maxWords)
            .joinToString(" ")
    }
}
