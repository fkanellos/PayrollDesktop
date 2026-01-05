package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.core.utils.StringNormalizer
import com.payroll.app.desktop.domain.models.ClientMatchResult
import com.payroll.app.desktop.domain.models.MatchConfidence

/**
 * Client Matching Service
 * Handles client name matching logic for calendar events with confidence levels
 *
 * Matching strategies (in order of confidence):
 * 1. EXACT: Special keywords match (if provided)
 * 2. EXACT: Full name match (first 2 words, ignoring extras like "Μετρητά", "Online")
 * 3. HIGH: Reversed name match (e.g., "John Doe" -> "Doe John")
 * 4. HIGH: Dash-separated name parts (e.g., "John - Γιάννης")
 * 5. MEDIUM: Surname match only (word boundary, min 4 chars) - REQUIRES CONFIRMATION
 * 6. LOW: First name match only (word boundary, min 4 chars) - REQUIRES CONFIRMATION
 *
 * All matching is case-insensitive and accent-insensitive
 */
class ClientMatchingService {

    /**
     * Find client matches with confidence levels
     *
     * @param title Event title to match against
     * @param clientNames List of client names to search
     * @param specialKeywords Special keywords that override normal matching
     * @return List of match results with confidence levels
     */
    fun findClientMatchesWithConfidence(
        title: String,
        clientNames: List<String>,
        specialKeywords: List<String> = emptyList()
    ): List<ClientMatchResult> {
        if (title.isBlank()) return emptyList()

        val titleNormalized = StringNormalizer.normalize(title)
        val matches = mutableListOf<ClientMatchResult>()

        println("🔍 MATCHING: '$title' → normalized: '$titleNormalized'")

        // Strategy 1: Special keywords (EXACT match)
        for (keyword in specialKeywords) {
            val keywordNormalized = StringNormalizer.normalize(keyword)
            if (keywordNormalized in titleNormalized) {
                matches.add(
                    ClientMatchResult(
                        clientName = keyword,
                        confidence = MatchConfidence.EXACT,
                        matchedText = keyword,
                        reason = "Ειδική λέξη-κλειδί (supervision/εποπτεία)"
                    )
                )
                return matches  // Special keywords have priority
            }
        }

        // Match against client names
        for (clientName in clientNames) {
            if (clientName.isBlank()) continue

            val clientNormalized = StringNormalizer.normalizeForMatching(clientName, maxWords = 2)
            val nameParts = clientNormalized.split(" ").filter { it.isNotBlank() }

            // Strategy 2: Full name match (EXACT)
            if (clientNormalized in titleNormalized) {
                matches.add(
                    ClientMatchResult(
                        clientName = clientName,
                        confidence = MatchConfidence.EXACT,
                        matchedText = clientNormalized,
                        reason = "Πλήρες όνομα (Όνομα + Επώνυμο)"
                    )
                )
                continue
            }

            // Single name - try direct match
            if (nameParts.size < 2) {
                val singleName = nameParts.firstOrNull() ?: continue
                if (singleName in titleNormalized) {
                    matches.add(
                        ClientMatchResult(
                            clientName = clientName,
                            confidence = MatchConfidence.MEDIUM,
                            matchedText = singleName,
                            reason = "Μονό όνομα"
                        )
                    )
                }
                continue
            }

            val firstName = nameParts.first()
            val surname = nameParts.last()

            // Strategy 3: Reversed name match (HIGH)
            val reversedName = "$surname $firstName"
            if (reversedName in titleNormalized) {
                matches.add(
                    ClientMatchResult(
                        clientName = clientName,
                        confidence = MatchConfidence.HIGH,
                        matchedText = reversedName,
                        reason = "Αντίστροφη σειρά (Επώνυμο + Όνομα)"
                    )
                )
                continue
            }

            // Strategy 4: Dash-separated names (HIGH)
            if ("-" in clientName) {
                val parts = clientName.split("-").map {
                    StringNormalizer.normalizeForMatching(it.trim(), maxWords = 2)
                }
                for (part in parts) {
                    if (part in titleNormalized) {
                        matches.add(
                            ClientMatchResult(
                                clientName = clientName,
                                confidence = MatchConfidence.HIGH,
                                matchedText = part,
                                reason = "Εναλλακτικό όνομα (με παύλα)"
                            )
                        )
                        break
                    }
                }
                if (matches.any { it.clientName == clientName }) continue
            }

            // Strategy 5: Surname match only - DISABLED
            // Reasoning: If only surname matches, it's likely a different person (e.g., siblings)
            // We only want to ask for confirmation when first name matches (same person, different spelling)
            // if (surname.length > 3) { ... } // REMOVED

            // Strategy 6: First name match only (LOW - needs confirmation)
            if (firstName.length > 3) {
                // Check if first name appears as a whole word (surrounded by spaces or start/end)
                val found = titleNormalized.contains(" $firstName ") ||
                           titleNormalized.startsWith("$firstName ") ||
                           titleNormalized.endsWith(" $firstName") ||
                           titleNormalized == firstName

                if (found) {
                    matches.add(
                        ClientMatchResult(
                            clientName = clientName,
                            confidence = MatchConfidence.LOW,
                            matchedText = firstName,
                            reason = "Μόνο όνομα (χρειάζεται επιβεβαίωση)"
                        )
                    )
                }
            }
        }

        return matches
    }

    /**
     * Legacy method - returns only client names (backwards compatible)
     * Uses only EXACT and HIGH confidence matches
     */
    fun findClientMatches(
        title: String,
        clientNames: List<String>,
        specialKeywords: List<String> = emptyList()
    ): List<String> {
        // Use new confidence-based matching
        val matchResults = findClientMatchesWithConfidence(title, clientNames, specialKeywords)

        // Return only EXACT and HIGH confidence matches
        return matchResults
            .filter { it.confidence == MatchConfidence.EXACT || it.confidence == MatchConfidence.HIGH }
            .map { it.clientName }
            .distinct()
    }

    /**
     * Get uncertain matches that require user confirmation
     * Returns matches with MEDIUM or LOW confidence
     */
    fun getUncertainMatches(
        title: String,
        clientNames: List<String>,
        specialKeywords: List<String> = emptyList()
    ): List<ClientMatchResult> {
        val matchResults = findClientMatchesWithConfidence(title, clientNames, specialKeywords)

        // Return only MEDIUM and LOW confidence matches
        return matchResults
            .filter { it.confidence == MatchConfidence.MEDIUM || it.confidence == MatchConfidence.LOW }
    }

    /**
     * Check if a match requires user confirmation
     */
    fun requiresConfirmation(matchResult: ClientMatchResult): Boolean {
        return matchResult.confidence == MatchConfidence.MEDIUM ||
                matchResult.confidence == MatchConfidence.LOW
    }
}
