package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.core.utils.roundToCents
import com.payroll.app.desktop.domain.models.*
import kotlinx.datetime.LocalDateTime

/**
 * Payroll Calculation Service
 * Core business logic for calculating employee payroll locally
 *
 * Note: This service now tracks uncertain matches that need user confirmation
 */
class PayrollCalculationService(
    private val clientMatchingService: ClientMatchingService = ClientMatchingService()
) {

    /**
     * Store uncertain matches found during calculation
     * These will be presented to the user for confirmation
     */
    data class UncertainMatchInfo(
        val event: CalendarEvent,
        val possibleMatches: List<ClientMatchResult>
    )

    /**
     * Calculate payroll for an employee based on calendar events
     *
     * @param employee The employee to calculate payroll for
     * @param clients List of clients belonging to this employee
     * @param events All calendar events from the period
     * @param periodStart Start of the payroll period
     * @param periodEnd End of the payroll period
     * @param supervisionConfig Optional configuration for supervision sessions
     * @return Complete payroll report
     */
    fun calculatePayroll(
        employee: Employee,
        clients: List<Client>,
        events: List<CalendarEvent>,
        periodStart: LocalDateTime,
        periodEnd: LocalDateTime,
        supervisionConfig: SupervisionConfig? = null
    ): PayrollReport {
        // Get client names for matching
        val clientNames = clients.map { it.name }
        val clientLookup = clients.associateBy { it.name }
        val specialKeywords = supervisionConfig?.keywords ?: emptyList()

        // Match events to clients
        val clientEvents = mutableMapOf<String, MutableList<CalendarEvent>>()
        val unmatchedEvents = mutableListOf<CalendarEvent>()
        val uncertainMatches = mutableListOf<UncertainMatch>()

        // Initialize map for all clients
        clientNames.forEach { clientEvents[it] = mutableListOf() }
        specialKeywords.forEach { clientEvents[it] = mutableListOf() }

        // Filter and match events
        for (event in events) {
            // Skip events outside the period
            if (!isEventInPeriod(event, periodStart, periodEnd)) continue

            // Skip empty titles
            if (event.title.isBlank()) continue

            // First try exact/high confidence matches
            val highConfidenceMatches = clientMatchingService.findClientMatches(
                event.title,
                clientNames,
                specialKeywords
            )

            if (highConfidenceMatches.isNotEmpty()) {
                // Exact or high confidence match found
                val matchedClient = highConfidenceMatches.first()
                clientEvents[matchedClient]?.add(event)
            } else {
                // Check for uncertain matches (MEDIUM/LOW confidence)
                val allMatches = clientMatchingService.findClientMatchesWithConfidence(
                    event.title,
                    clientNames,
                    specialKeywords
                )

                val uncertainCandidates = allMatches.filter {
                    it.confidence == MatchConfidence.MEDIUM || it.confidence == MatchConfidence.LOW
                }

                if (uncertainCandidates.isNotEmpty()) {
                    // Uncertain match - needs user confirmation
                    uncertainMatches.add(
                        UncertainMatch(
                            eventTitle = event.title,
                            calendarEventId = event.id,
                            possibleMatches = uncertainCandidates,
                            suggestedMatch = uncertainCandidates.first()  // Highest confidence among uncertain
                        )
                    )
                } else {
                    // No matches at all
                    unmatchedEvents.add(event)
                }
            }
        }

        // Calculate payroll entries
        val entries = mutableListOf<PayrollEntry>()
        var totalSessions = 0
        var totalRevenue = 0.0
        var totalEmployeeEarnings = 0.0
        var totalCompanyEarnings = 0.0

        // Process client events
        for ((clientName, clientEventList) in clientEvents) {
            // Skip supervision keywords (handled separately)
            if (supervisionConfig != null && clientName in supervisionConfig.keywords) {
                continue
            }

            val client = clientLookup[clientName] ?: continue

            // Filter valid events (not red cancelled)
            val validEvents = clientEventList.filter { event ->
                !event.isCancelled || event.isPendingPayment
            }

            if (validEvents.isNotEmpty()) {
                val sessionsCount = validEvents.size
                val clientRevenue = (sessionsCount * client.price).roundToCents()
                val employeeEarnings = (sessionsCount * client.employeePrice).roundToCents()
                val companyEarnings = (sessionsCount * client.companyPrice).roundToCents()

                val entry = PayrollEntry(
                    clientName = clientName,
                    clientPrice = client.price,
                    employeePrice = client.employeePrice,
                    companyPrice = client.companyPrice,
                    sessionsCount = sessionsCount,
                    totalRevenue = clientRevenue,
                    employeeEarnings = employeeEarnings,
                    companyEarnings = companyEarnings,
                    events = validEvents
                )

                entries.add(entry)
                totalSessions += sessionsCount
                totalRevenue = (totalRevenue + clientRevenue).roundToCents()
                totalEmployeeEarnings = (totalEmployeeEarnings + employeeEarnings).roundToCents()
                totalCompanyEarnings = (totalCompanyEarnings + companyEarnings).roundToCents()
            }
        }

        // Process supervision sessions
        if (supervisionConfig != null && supervisionConfig.enabled) {
            val allSupervisionEvents = mutableListOf<CalendarEvent>()

            for (keyword in supervisionConfig.keywords) {
                val supervisionEvents = clientEvents[keyword] ?: emptyList()
                allSupervisionEvents.addAll(supervisionEvents.filter { !it.isCancelled || it.isPendingPayment })
            }

            if (allSupervisionEvents.isNotEmpty()) {
                val sessionsCount = allSupervisionEvents.size
                val clientRevenue = (sessionsCount * supervisionConfig.price).roundToCents()
                val employeeEarnings = (sessionsCount * supervisionConfig.employeePrice).roundToCents()
                val companyEarnings = (sessionsCount * supervisionConfig.companyPrice).roundToCents()

                val entry = PayrollEntry(
                    clientName = "Εποπτεία (Supervision)",
                    clientPrice = supervisionConfig.price,
                    employeePrice = supervisionConfig.employeePrice,
                    companyPrice = supervisionConfig.companyPrice,
                    sessionsCount = sessionsCount,
                    totalRevenue = clientRevenue,
                    employeeEarnings = employeeEarnings,
                    companyEarnings = companyEarnings,
                    events = allSupervisionEvents
                )

                entries.add(entry)
                totalSessions += sessionsCount
                totalRevenue = (totalRevenue + clientRevenue).roundToCents()
                totalEmployeeEarnings = (totalEmployeeEarnings + employeeEarnings).roundToCents()
                totalCompanyEarnings = (totalCompanyEarnings + companyEarnings).roundToCents()
            }
        }

        return PayrollReport(
            employee = employee,
            periodStart = periodStart,
            periodEnd = periodEnd,
            entries = entries,
            totalSessions = totalSessions,
            totalRevenue = totalRevenue,
            totalEmployeeEarnings = totalEmployeeEarnings,
            totalCompanyEarnings = totalCompanyEarnings,
            unmatchedEvents = unmatchedEvents,
            uncertainMatches = uncertainMatches
        )
    }

    /**
     * Check if event falls within the period
     */
    private fun isEventInPeriod(
        event: CalendarEvent,
        periodStart: LocalDateTime,
        periodEnd: LocalDateTime
    ): Boolean {
        return event.startTime >= periodStart && event.startTime <= periodEnd
    }
}
