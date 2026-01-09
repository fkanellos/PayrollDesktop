package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.domain.models.*
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

/**
 * Unit Tests for PayrollCalculationService
 * Tests core business logic for payroll calculations
 */
class PayrollCalculationServiceTest {

    private lateinit var service: PayrollCalculationService
    private lateinit var testEmployee: Employee

    @BeforeTest
    fun setup() {
        service = PayrollCalculationService()
        testEmployee = Employee(
            id = "emp1",
            name = "John Doe",
            email = "john@example.com",
            calendarId = "cal123",
            sheetName = "John's Sheet",
            supervisionPrice = 50.0,
            color = "#2196F3"
        )
    }

    @Test
    fun `calculatePayroll with single client - correct totals`() {
        // Given
        val client = Client(
            id = 1L,
            name = "Client A",
            price = 50.0,
            employeePrice = 30.0,
            companyPrice = 20.0,
            employeeId = "emp1",
            pendingPayment = false
        )

        val events = listOf(
            createEvent(id = "e1", title = "Client A Session", cancelled = false),
            createEvent(id = "e2", title = "Client A Session", cancelled = false),
            createEvent(id = "e3", title = "Client A Session", cancelled = false)
        )

        val periodStart = LocalDateTime(2024, 1, 1, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 31, 23, 59)

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(client),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        // Then
        assertEquals(3, report.totalSessions, "Should count 3 sessions")
        assertEquals(150.0, report.totalRevenue, "Total revenue = 3 * €50")
        assertEquals(90.0, report.totalEmployeeEarnings, "Employee earnings = 3 * €30")
        assertEquals(60.0, report.totalCompanyEarnings, "Company earnings = 3 * €20")
        assertEquals(1, report.entries.size, "Should have 1 payroll entry")
        assertEquals(0, report.unmatchedEvents.size, "Should have no unmatched events")
    }

    @Test
    fun `calculatePayroll with multiple clients - correct breakdown`() {
        // Given
        val clientA = Client(
            id = 1L, name = "Client A", price = 50.0,
            employeePrice = 30.0, companyPrice = 20.0,
            employeeId = "emp1", pendingPayment = false
        )
        val clientB = Client(
            id = 2L, name = "Client B", price = 60.0,
            employeePrice = 35.0, companyPrice = 25.0,
            employeeId = "emp1", pendingPayment = false
        )

        val events = listOf(
            createEvent(id = "e1", title = "Client A Session"),
            createEvent(id = "e2", title = "Client A Session"),
            createEvent(id = "e3", title = "Client B Session")
        )

        val periodStart = LocalDateTime(2024, 1, 1, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 31, 23, 59)

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(clientA, clientB),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        // Then
        assertEquals(3, report.totalSessions)
        assertEquals(160.0, report.totalRevenue, "Total = 2*€50 + 1*€60")
        assertEquals(95.0, report.totalEmployeeEarnings, "Employee = 2*€30 + 1*€35")
        assertEquals(65.0, report.totalCompanyEarnings, "Company = 2*€20 + 1*€25")
        assertEquals(2, report.entries.size, "Should have 2 entries (one per client)")
    }

    @Test
    fun `calculatePayroll excludes cancelled events`() {
        // Given
        val client = Client(
            id = 1L, name = "Client A", price = 50.0,
            employeePrice = 30.0, companyPrice = 20.0,
            employeeId = "emp1", pendingPayment = false
        )

        val events = listOf(
            createEvent(id = "e1", title = "Client A Session", cancelled = false),
            createEvent(id = "e2", title = "Client A Session", cancelled = true), // Cancelled - should be excluded
            createEvent(id = "e3", title = "Client A Session", cancelled = false)
        )

        val periodStart = LocalDateTime(2024, 1, 1, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 31, 23, 59)

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(client),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        // Then
        assertEquals(2, report.totalSessions, "Should only count non-cancelled sessions")
        assertEquals(100.0, report.totalRevenue, "Total = 2 * €50 (cancelled excluded)")
    }

    @Test
    fun `calculatePayroll includes pending payment events`() {
        // Given
        val client = Client(
            id = 1L, name = "Client A", price = 50.0,
            employeePrice = 30.0, companyPrice = 20.0,
            employeeId = "emp1", pendingPayment = false
        )

        val events = listOf(
            createEvent(id = "e1", title = "Client A Session", cancelled = false),
            createEvent(id = "e2", title = "Client A Session", cancelled = true, pendingPayment = true) // Cancelled but pending
        )

        val periodStart = LocalDateTime(2024, 1, 1, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 31, 23, 59)

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(client),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        // Then
        assertEquals(2, report.totalSessions, "Should count pending payment events")
        assertEquals(100.0, report.totalRevenue, "Total = 2 * €50 (pending included)")
    }

    @Test
    fun `calculatePayroll tracks unmatched events`() {
        // Given
        val client = Client(
            id = 1L, name = "Client A", price = 50.0,
            employeePrice = 30.0, companyPrice = 20.0,
            employeeId = "emp1", pendingPayment = false
        )

        val events = listOf(
            createEvent(id = "e1", title = "Client A Session"),
            createEvent(id = "e2", title = "Unknown Client XYZ") // No match
        )

        val periodStart = LocalDateTime(2024, 1, 1, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 31, 23, 59)

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(client),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        // Then
        assertEquals(1, report.totalSessions, "Should only count matched events")
        assertEquals(1, report.unmatchedEvents.size, "Should track 1 unmatched event")
        assertEquals("Unknown Client XYZ", report.unmatchedEvents[0].title)
    }

    @Test
    fun `calculatePayroll with supervision sessions`() {
        // Given
        val client = Client(
            id = 1L, name = "Client A", price = 50.0,
            employeePrice = 30.0, companyPrice = 20.0,
            employeeId = "emp1", pendingPayment = false
        )

        val supervisionConfig = SupervisionConfig(
            enabled = true,
            keywords = listOf("supervision", "εποπτεία"),
            price = 80.0,
            employeePrice = 50.0,
            companyPrice = 30.0
        )

        val events = listOf(
            createEvent(id = "e1", title = "Client A Session"),
            createEvent(id = "e2", title = "Supervision Meeting")
        )

        val periodStart = LocalDateTime(2024, 1, 1, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 31, 23, 59)

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(client),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd,
            supervisionConfig = supervisionConfig
        )

        // Then
        assertEquals(2, report.totalSessions, "Should count client + supervision")
        assertEquals(130.0, report.totalRevenue, "Total = €50 + €80")
        assertEquals(80.0, report.totalEmployeeEarnings, "Employee = €30 + €50")
        assertEquals(50.0, report.totalCompanyEarnings, "Company = €20 + €30")
        assertEquals(2, report.entries.size, "Should have 2 entries (client + supervision)")
    }

    @Test
    fun `calculatePayroll filters events outside period`() {
        // Given
        val client = Client(
            id = 1L, name = "Client A", price = 50.0,
            employeePrice = 30.0, companyPrice = 20.0,
            employeeId = "emp1", pendingPayment = false
        )

        val periodStart = LocalDateTime(2024, 1, 10, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 20, 23, 59)

        val events = listOf(
            createEvent(id = "e1", title = "Client A", date = LocalDateTime(2024, 1, 5, 10, 0)), // Before period
            createEvent(id = "e2", title = "Client A", date = LocalDateTime(2024, 1, 15, 10, 0)), // Inside period
            createEvent(id = "e3", title = "Client A", date = LocalDateTime(2024, 1, 25, 10, 0))  // After period
        )

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(client),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        // Then
        assertEquals(1, report.totalSessions, "Should only count event inside period")
        assertEquals(50.0, report.totalRevenue)
    }

    @Test
    fun `calculatePayroll with currency precision - rounds correctly`() {
        // Given
        val client = Client(
            id = 1L, name = "Client A",
            price = 33.33, // Will cause floating point precision issues
            employeePrice = 20.20,
            companyPrice = 13.13,
            employeeId = "emp1", pendingPayment = false
        )

        val events = listOf(
            createEvent(id = "e1", title = "Client A"),
            createEvent(id = "e2", title = "Client A"),
            createEvent(id = "e3", title = "Client A")
        )

        val periodStart = LocalDateTime(2024, 1, 1, 0, 0)
        val periodEnd = LocalDateTime(2024, 1, 31, 23, 59)

        // When
        val report = service.calculatePayroll(
            employee = testEmployee,
            clients = listOf(client),
            events = events,
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        // Then
        assertEquals(99.99, report.totalRevenue, "Should round to 2 decimals")
        assertEquals(60.6, report.totalEmployeeEarnings, "Should round to 2 decimals")
        assertEquals(39.39, report.totalCompanyEarnings, "Should round to 2 decimals")
    }

    // Helper function to create test events
    private fun createEvent(
        id: String,
        title: String,
        date: LocalDateTime = LocalDateTime(2024, 1, 15, 10, 0),
        cancelled: Boolean = false,
        pendingPayment: Boolean = false
    ): CalendarEvent {
        return CalendarEvent(
            id = id,
            title = title,
            startTime = date,
            endTime = date.let { LocalDateTime(it.year, it.month, it.dayOfMonth, it.hour + 1, it.minute) },
            attendees = emptyList(),
            isCancelled = cancelled,
            isPendingPayment = pendingPayment
        )
    }
}
