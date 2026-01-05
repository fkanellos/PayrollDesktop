package com.payroll.app.desktop.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Employee domain model
 * Updated to match backend API
 */
@Serializable
data class Employee(
    val id: String,
    val name: String,
    val email: String = "",
    @SerialName("calendarId") val calendarId: String,
    val color: String = "#2196F3",
    @SerialName("sheetName") val sheetName: String = "",
    @SerialName("supervisionPrice") val supervisionPrice: Double = 0.0
)

/**
 * Client domain model
 */
@Serializable
data class Client(
    val id: Long = 0,
    val name: String,
    val price: Double,
    @SerialName("employeePrice") val employeePrice: Double,
    @SerialName("companyPrice") val companyPrice: Double,
    @SerialName("employeeId") val employeeId: String,
    @SerialName("pendingPayment") val pendingPayment: Boolean = false
)

/**
 * Payroll calculation request
 */
@Serializable
data class PayrollRequest(
    val employeeId: String,
    val startDate: String, // ISO format: 2024-09-01T00:00:00
    val endDate: String     // ISO format: 2024-09-30T23:59:59
)

/**
 * Individual client payroll detail
 * Updated with pending payment tracking fields
 */
@Serializable
data class ClientPayrollDetail(
    val clientName: String,
    val pricePerSession: Double,
    val employeePricePerSession: Double,
    val companyPricePerSession: Double,
    val sessions: Int,
    val totalRevenue: Double,
    val employeeEarnings: Double,
    val companyEarnings: Double,
    val eventDetails: List<EventDetail> = emptyList(),
    // Pending payment tracking fields
    val completedSessions: Int = 0,          // Completed in current period
    val pendingSessions: Int = 0,             // Pending in current period (grey, not paid yet)
    val paidPendingCount: Int = 0,            // Paid from previous period
    val unresolvedPendingCount: Int = 0       // Still owe from previous
)

/**
 * Event detail for audit trail
 * Updated with pending payment fields
 */
@Serializable
data class EventDetail(
    val date: String,
    val time: String,
    val duration: String,
    val status: String, // "completed", "cancelled", "pending_payment", "paid_for_pending"
    val colorId: String? = null,
    // Pending payment fields
    val isPending: Boolean = false,           // Is this a pending payment (grey)?
    val paidForPending: Boolean = false,      // Did this session pay for a previous pending?
    val pendingDate: String? = null           // Date of the pending it paid for
)

/**
 * Payroll summary data
 */
@Serializable
data class PayrollSummary(
    val totalSessions: Int,
    val totalRevenue: Double,
    val employeeEarnings: Double,
    val companyEarnings: Double
)

/**
 * Employee info for reports
 */
@Serializable
data class EmployeeInfo(
    val id: String,
    val name: String,
    val email: String
)

/**
 * Complete payroll response from backend
 * Updated with event tracking
 */
@Serializable
data class PayrollResponse(
    val id: String? = null,
    val employee: EmployeeInfo,
    val period: String,
    val summary: PayrollSummary,
    val clientBreakdown: List<ClientPayrollDetail>,
    val generatedAt: String,
    // Event tracking for unmatched events, etc.
    val eventTracking: EventTracking? = null
)

// ============================================================
// EVENT TRACKING MODELS
// ============================================================

/**
 * Event tracking information from backend
 */
@Serializable
data class EventTracking(
    val totalEvents: Int = 0,
    val matchedEvents: Int = 0,
    val unmatchedEvents: List<UnmatchedEvent> = emptyList(),
    val uncertainMatches: List<UncertainMatch> = emptyList(),  // Events needing user confirmation
    val cancelledGrey: List<CancelledEvent> = emptyList(),
    val cancelledRed: List<CancelledEvent> = emptyList(),
    val supervision: List<SupervisionEvent> = emptyList(),
    val emptyTitle: Int = 0
)

/**
 * Unmatched event - name doesn't match any client in database
 */
@Serializable
data class UnmatchedEvent(
    val title: String,
    val date: String,
    val time: String,
    val colorId: String? = null,
    val status: String = ""
)

/**
 * Cancelled event (grey or red)
 */
@Serializable
data class CancelledEvent(
    val title: String,
    val date: String,
    val time: String,
    val colorId: String,
    val isPending: Boolean = false
)

/**
 * Supervision event with special pricing
 */
@Serializable
data class SupervisionEvent(
    val title: String,
    val date: String,
    val time: String,
    val duration: String
)