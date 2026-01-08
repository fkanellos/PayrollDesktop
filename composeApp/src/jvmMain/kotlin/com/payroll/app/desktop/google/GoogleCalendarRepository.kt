package com.payroll.app.desktop.google

import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.RetryUtils

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.payroll.app.desktop.data.repositories.CalendarRepository
import com.payroll.app.desktop.domain.models.CalendarColors
import com.payroll.app.desktop.domain.models.CalendarEvent
import com.payroll.app.desktop.domain.models.CalendarInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.ZoneId

/**
 * Google Calendar implementation of CalendarRepository
 * Fetches events directly from Google Calendar API
 */
class GoogleCalendarRepository(
    private val credentialProvider: GoogleCredentialProvider
) : CalendarRepository {

    companion object {
        private const val TAG = "GoogleCalendarRepository"
    }

    private var calendarService: Calendar? = null

    init {
        try {
            calendarService = credentialProvider.getCalendarService()
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to initialize Google Calendar", e)
        }
    }

    override fun isAvailable(): Boolean = calendarService != null

    override suspend fun authenticate() {
        withContext(Dispatchers.IO) {
            credentialProvider.authenticate()
            calendarService = credentialProvider.getCalendarService()
        }
    }

    override suspend fun clearCredentials() {
        withContext(Dispatchers.IO) {
            credentialProvider.deleteCredentials()
            calendarService = null
        }
    }

    override suspend fun getEventsForPeriod(
        calendarId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val service = calendarService ?: return@withContext emptyList()

        try {
            val timeMin = DateTime(
                java.time.LocalDateTime.of(
                    startDate.year, startDate.monthNumber, startDate.dayOfMonth,
                    startDate.hour, startDate.minute, startDate.second
                ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )

            val timeMax = DateTime(
                java.time.LocalDateTime.of(
                    endDate.year, endDate.monthNumber, endDate.dayOfMonth,
                    endDate.hour, endDate.minute, endDate.second
                ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )

            // Use retry logic for API call
            val events = RetryUtils.retryWithBackoff {
                service.events().list(calendarId)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .setMaxResults(2500)
                    .execute()
            }

            events.items?.mapNotNull { event ->
                try {
                    val eventStartTime = if (event.start?.dateTime != null) {
                        java.time.LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(event.start.dateTime.value),
                            ZoneId.systemDefault()
                        ).toKotlinLocalDateTime()
                    } else if (event.start?.date != null) {
                        java.time.LocalDateTime.parse(event.start.date.toString() + "T00:00:00")
                            .toKotlinLocalDateTime()
                    } else {
                        return@mapNotNull null
                    }

                    val eventEndTime = if (event.end?.dateTime != null) {
                        java.time.LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(event.end.dateTime.value),
                            ZoneId.systemDefault()
                        ).toKotlinLocalDateTime()
                    } else if (event.end?.date != null) {
                        java.time.LocalDateTime.parse(event.end.date.toString() + "T23:59:59")
                            .toKotlinLocalDateTime()
                    } else {
                        return@mapNotNull null
                    }

                    val colorId = event.colorId
                    val isCancelled = event.status == "cancelled" ||
                            (colorId == CalendarColors.RED_CANCELLED && !isSupervision(event.summary ?: ""))
                    val isPendingPayment = colorId == CalendarColors.GREY_CANCELLED

                    CalendarEvent(
                        id = event.id ?: "",
                        title = event.summary ?: "Χωρίς τίτλο",
                        startTime = eventStartTime,
                        endTime = eventEndTime,
                        colorId = colorId,
                        isCancelled = isCancelled,
                        isPendingPayment = isPendingPayment,
                        attendees = event.attendees?.mapNotNull { it.email } ?: emptyList()
                    )
                } catch (e: Exception) {
                    Logger.warning(TAG, "Error parsing event: ${e.message}")
                    null
                }
            } ?: emptyList()

        } catch (e: Exception) {
            Logger.error(TAG, "Error fetching calendar events: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getCalendarList(): List<CalendarInfo> = withContext(Dispatchers.IO) {
        val service = calendarService ?: return@withContext emptyList()

        try {
            // Use retry logic for API call
            val calendarList = RetryUtils.retryWithBackoff {
                service.calendarList().list().execute()
            }
            calendarList.items?.map { calendar ->
                CalendarInfo(
                    id = calendar.id ?: "",
                    name = calendar.summary ?: "",
                    isPrimary = calendar.primary ?: false
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Logger.error(TAG, "Error fetching calendar list: ${e.message}")
            emptyList()
        }
    }

    /**
     * Check if event is a supervision session
     */
    private fun isSupervision(summary: String): Boolean {
        val normalized = summary.lowercase().trim()
            .replace("ά", "α").replace("έ", "ε")
            .replace("ή", "η").replace("ί", "ι")
            .replace("ό", "ο").replace("ύ", "υ")
            .replace("ώ", "ω")
        return normalized == Strings.Keywords.supervision || normalized == Strings.Keywords.supervisionEnglish
    }
}
