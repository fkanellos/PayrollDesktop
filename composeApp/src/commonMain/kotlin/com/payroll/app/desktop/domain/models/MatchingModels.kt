package com.payroll.app.desktop.domain.models

import kotlinx.serialization.Serializable

/**
 * Match confidence levels
 */
enum class MatchConfidence {
    EXACT,           // Full name match (both first + last name)
    HIGH,            // Reversed name or dash-separated match
    MEDIUM,          // Surname match only (needs confirmation)
    LOW,             // First name match only (needs confirmation)
    NONE             // No match
}

/**
 * Result of a client matching operation
 */
@Serializable
data class ClientMatchResult(
    val clientName: String,
    val confidence: MatchConfidence,
    val matchedText: String,      // The part of the event title that matched
    val reason: String            // Human-readable explanation
)

/**
 * Uncertain match that requires user confirmation
 */
@Serializable
data class UncertainMatch(
    val eventTitle: String,
    val calendarEventId: String,
    val possibleMatches: List<ClientMatchResult>,
    val suggestedMatch: ClientMatchResult?  // The best guess (highest confidence)
)

/**
 * User's decision on an uncertain match
 */
@Serializable
data class MatchConfirmation(
    val eventTitle: String,
    val calendarEventId: String,
    val selectedClientName: String?,  // null = create new client
    val createNew: Boolean = false,
    val newClientData: Client? = null  // If creating new client
)
