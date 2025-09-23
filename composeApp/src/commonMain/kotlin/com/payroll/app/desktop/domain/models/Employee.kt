package com.payroll.app.desktop.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Employee domain model
 */
@Serializable
data class Employee(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("calendarId") val calendarId: String,
    val color: String = "#2196F3"
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
    val eventDetails: List<EventDetail> = emptyList()
)

/**
 * Event detail for audit trail
 */
@Serializable
data class EventDetail(
    val date: String,
    val time: String,
    val duration: String,
    val status: String, // "completed", "cancelled", "pending_payment"
    val colorId: String? = null
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
 */
@Serializable
data class PayrollResponse(
    val employee: EmployeeInfo,
    val period: String,
    val summary: PayrollSummary,
    val clientBreakdown: List<ClientPayrollDetail>,
    val generatedAt: String
)