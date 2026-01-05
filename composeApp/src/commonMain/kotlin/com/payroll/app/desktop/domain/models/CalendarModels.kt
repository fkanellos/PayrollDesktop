package com.payroll.app.desktop.domain.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Calendar Event from Google Calendar
 * Represents a therapy session
 */
@Serializable
data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val colorId: String? = null,
    val isCancelled: Boolean = false,
    val isPendingPayment: Boolean = false,
    val attendees: List<String> = emptyList()
)

/**
 * Color IDs for cancellation detection
 */
object CalendarColors {
    const val GREY_CANCELLED = "8"   // Cancelled but client will pay next time
    const val RED_CANCELLED = "11"   // Cancelled, should NOT be paid
}

/**
 * Basic calendar info
 */
@Serializable
data class CalendarInfo(
    val id: String,
    val name: String,
    val isPrimary: Boolean = false
)
