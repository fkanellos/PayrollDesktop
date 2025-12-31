package com.payroll.app.desktop.domain.service

/**
 * Client Matching Service
 * Handles client name matching logic for calendar events
 *
 * Matching strategies (in order):
 * 1. Special keywords match (if provided)
 * 2. Full name match (exact substring)
 * 3. Reversed name match (e.g., "John Doe" -> "Doe John")
 * 4. Surname match (word boundary, min 4 chars)
 * 5. First name match (word boundary, min 4 chars)
 * 6. Dash-separated name parts (e.g., "John - Γιάννης")
 */
class ClientMatchingService {

    /**
     * Find client matches for a given event title
     *
     * @param title Event title to match against
     * @param clientNames List of client names to search
     * @param specialKeywords Special keywords that override normal matching
     * @return List of matching client names
     */
    fun findClientMatches(
        title: String,
        clientNames: List<String>,
        specialKeywords: List<String> = emptyList()
    ): List<String> {
        if (title.isBlank()) return emptyList()

        // Normalize title (lowercase + remove Greek accents)
        val titleLower = normalizeText(title)
        val matches = mutableListOf<String>()

        // Strategy 1: Special keywords (e.g., "supervision")
        for (keyword in specialKeywords) {
            if (normalizeText(keyword) in titleLower) {
                matches.add(keyword)
                return matches
            }
        }

        // Match against client names
        for (clientName in clientNames) {
            if (clientName.isBlank()) continue

            val clientLower = normalizeText(clientName)
            val nameParts = clientLower.split(" ").filter { it.isNotBlank() }

            // Strategy 2: Full name match
            if (clientLower in titleLower) {
                matches.add(clientName)
                continue
            }

            // If single name, try direct match
            if (nameParts.size < 2) {
                if (nameParts.firstOrNull()?.let { it in titleLower } == true) {
                    matches.add(clientName)
                }
                continue
            }

            // Strategy 3: Reversed name match
            val reversedName = "${nameParts.last()} ${nameParts.first()}"
            if (reversedName in titleLower) {
                matches.add(clientName)
                continue
            }

            // Strategy 4: Surname match (min 4 chars)
            val surname = nameParts.last()
            if (surname.length > 3) {
                val regex = "\\b${Regex.escape(surname)}\\b".toRegex()
                if (regex.find(titleLower) != null) {
                    matches.add(clientName)
                    continue
                }
            }

            // Strategy 5: First name match (min 4 chars)
            val firstName = nameParts.first()
            if (firstName.length > 3) {
                val regex = "\\b${Regex.escape(firstName)}\\b".toRegex()
                if (regex.find(titleLower) != null) {
                    matches.add(clientName)
                    continue
                }
            }

            // Strategy 6: Dash-separated names (e.g., "Ndrekaj Ornela - Ντρεκαι Ορνελα")
            if ("-" in clientName) {
                val parts = clientName.split("-").map { normalizeText(it.trim()) }
                for (part in parts) {
                    if (part in titleLower) {
                        matches.add(clientName)
                        break
                    }
                }
            }
        }

        return matches
    }

    /**
     * Normalize text for matching (lowercase + remove Greek accents)
     */
    private fun normalizeText(text: String): String {
        return text.lowercase().trim()
            .replace("ά", "α").replace("έ", "ε")
            .replace("ή", "η").replace("ί", "ι")
            .replace("ό", "ο").replace("ύ", "υ")
            .replace("ώ", "ω").replace("ΐ", "ι")
            .replace("ΰ", "υ")
    }
}
