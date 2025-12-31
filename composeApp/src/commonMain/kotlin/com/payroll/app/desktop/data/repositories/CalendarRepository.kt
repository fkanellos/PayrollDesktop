package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.domain.models.CalendarEvent
import com.payroll.app.desktop.domain.models.CalendarInfo
import kotlinx.datetime.LocalDateTime

/**
 * Repository interface for Calendar data access
 * Implementation will use Google Calendar API for desktop
 */
interface CalendarRepository {
    /**
     * Get calendar events for a specific period
     */
    suspend fun getEventsForPeriod(
        calendarId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CalendarEvent>

    /**
     * Get list of available calendars
     */
    suspend fun getCalendarList(): List<CalendarInfo>

    /**
     * Check if calendar service is available/authenticated
     */
    fun isAvailable(): Boolean

    /**
     * Re-authenticate with Google Calendar
     */
    suspend fun authenticate()

    /**
     * Clear stored credentials (force re-authentication)
     */
    suspend fun clearCredentials()
}
