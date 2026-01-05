package com.payroll.app.desktop.domain.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

/**
 * Single entry in payroll report (one client's sessions)
 * Used for local calculation
 */
@Serializable
data class PayrollEntry(
    val clientName: String,
    val clientPrice: Double,
    val employeePrice: Double,
    val companyPrice: Double,
    val sessionsCount: Int,
    val totalRevenue: Double,
    val employeeEarnings: Double,
    val companyEarnings: Double,
    val events: List<CalendarEvent> = emptyList()
)

/**
 * Complete payroll report for an employee
 * Used for local calculation
 */
@Serializable
data class PayrollReport(
    val employee: Employee,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val entries: List<PayrollEntry>,
    val totalSessions: Int,
    val totalRevenue: Double,
    val totalEmployeeEarnings: Double,
    val totalCompanyEarnings: Double,
    val unmatchedEvents: List<CalendarEvent> = emptyList(),
    val uncertainMatches: List<UncertainMatch> = emptyList(),  // Events that need user confirmation
    val generatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
) {
    /**
     * Convert to PayrollResponse for UI compatibility
     */
    fun toPayrollResponse(): PayrollResponse {
        return PayrollResponse(
            id = null,
            employee = EmployeeInfo(
                id = employee.id,
                name = employee.name,
                email = employee.email
            ),
            period = "${periodStart.date} - ${periodEnd.date}",
            summary = PayrollSummary(
                totalSessions = totalSessions,
                totalRevenue = totalRevenue,
                employeeEarnings = totalEmployeeEarnings,
                companyEarnings = totalCompanyEarnings
            ),
            clientBreakdown = entries.map { entry ->
                ClientPayrollDetail(
                    clientName = entry.clientName,
                    pricePerSession = entry.clientPrice,
                    employeePricePerSession = entry.employeePrice,
                    companyPricePerSession = entry.companyPrice,
                    sessions = entry.sessionsCount,
                    totalRevenue = entry.totalRevenue,
                    employeeEarnings = entry.employeeEarnings,
                    companyEarnings = entry.companyEarnings,
                    eventDetails = entry.events.map { event ->
                        EventDetail(
                            date = event.startTime.date.toString(),
                            time = "${event.startTime.hour}:${event.startTime.minute.toString().padStart(2, '0')}",
                            duration = calculateDuration(event),
                            status = when {
                                event.isPendingPayment -> "pending_payment"
                                event.isCancelled -> "cancelled"
                                else -> "completed"
                            },
                            colorId = event.colorId,
                            isPending = event.isPendingPayment,
                            paidForPending = false,
                            pendingDate = null
                        )
                    }
                )
            },
            generatedAt = generatedAt.toString(),
            eventTracking = EventTracking(
                totalEvents = entries.sumOf { it.events.size } + unmatchedEvents.size + uncertainMatches.size,
                matchedEvents = entries.sumOf { it.events.size },
                unmatchedEvents = unmatchedEvents.map { event ->
                    UnmatchedEvent(
                        title = event.title,
                        date = event.startTime.date.toString(),
                        time = "${event.startTime.hour}:${event.startTime.minute.toString().padStart(2, '0')}",
                        colorId = event.colorId
                    )
                },
                uncertainMatches = uncertainMatches
            )
        )
    }

    private fun calculateDuration(event: CalendarEvent): String {
        val minutes = (event.endTime.hour * 60 + event.endTime.minute) -
                (event.startTime.hour * 60 + event.startTime.minute)
        return "${minutes}min"
    }
}

/**
 * Configuration for supervision sessions
 */
@Serializable
data class SupervisionConfig(
    val enabled: Boolean = true,
    val price: Double = 0.0,
    val employeePrice: Double = 0.0,
    val companyPrice: Double = 0.0,
    val keywords: List<String> = listOf("Εποπτεία", "Supervision", "εποπτεια", "supervision")
)
