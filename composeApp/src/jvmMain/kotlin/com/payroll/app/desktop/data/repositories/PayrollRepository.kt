package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.logging.Logger

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.*
import com.payroll.app.desktop.domain.service.PayrollCalculationService
import com.payroll.app.desktop.google.GoogleSheetsService

/**
 * JVM implementation of PayrollRepository using local services
 */
actual class PayrollRepository(
    private val localEmployeeRepo: LocalEmployeeRepository,
    private val localClientRepo: LocalClientRepository,
    private val calendarRepo: CalendarRepository,
    private val payrollCalculationService: PayrollCalculationService,
    private val googleSheetsService: GoogleSheetsService,
    private val matchConfirmationRepo: com.payroll.app.desktop.presentation.payroll.IMatchConfirmationRepository
) {
    actual suspend fun getAllEmployees(): RepositoryResult<List<Employee>> {
        return try {
            val employees = localEmployeeRepo.getAll()
            RepositoryResult.Success(employees)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun getEmployee(id: String): RepositoryResult<Employee> {
        return try {
            val employee = localEmployeeRepo.getById(id)
            if (employee != null) {
                RepositoryResult.Success(employee)
            } else {
                RepositoryResult.Error(Exception("Employee not found: $id"))
            }
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return try {
            val clients = localClientRepo.getByEmployeeId(employeeId)
            RepositoryResult.Success(clients)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun calculatePayroll(request: PayrollRequest): RepositoryResult<PayrollResponse> {
        return try {
            // Get employee
            val employee = localEmployeeRepo.getById(request.employeeId)
                ?: return RepositoryResult.Error(Exception("Employee not found: ${request.employeeId}"))

            // Get employee's clients from local DB
            val clients = localClientRepo.getByEmployeeId(request.employeeId)

            // Parse dates
            val startDateTime = kotlinx.datetime.LocalDateTime.parse(request.startDate)
            val endDateTime = kotlinx.datetime.LocalDateTime.parse(request.endDate)

            // Get calendar events
            val events = calendarRepo.getEventsForPeriod(
                calendarId = employee.calendarId,
                startDate = startDateTime,
                endDate = endDateTime
            )

            // Create supervision config if employee has supervision price
            val supervisionConfig = if (employee.supervisionPrice > 0.0) {
                SupervisionConfig(
                    enabled = true,
                    price = employee.supervisionPrice,
                    employeePrice = employee.supervisionPrice * 0.4, // 40% for employee
                    companyPrice = employee.supervisionPrice * 0.6,  // 60% for company
                    keywords = listOf("εποπτεία", "supervision", "επόπτευση", "supervise")
                )
            } else null

            // Calculate payroll using PayrollCalculationService
            val payrollReport = payrollCalculationService.calculatePayroll(
                employee = employee,
                clients = clients,
                events = events,
                periodStart = startDateTime,
                periodEnd = endDateTime,
                supervisionConfig = supervisionConfig
            )

            // Convert to PayrollResponse
            val payrollResponse = payrollReport.toPayrollResponse()

            RepositoryResult.Success(payrollResponse)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun createClient(client: Client): RepositoryResult<Client> {
        return try {
            // Validate client data before inserting
            val existingClients = localClientRepo.getByEmployeeId(client.employeeId)
            val validationResult = com.payroll.app.desktop.domain.validation.ClientValidator.validateClient(
                client.toClientSimple(),
                existingClients.map { it.toClientSimple() }
            )

            if (validationResult is com.payroll.app.desktop.domain.validation.ValidationResult.Invalid) {
                val errorMessage = validationResult.errors.joinToString(", ") { it.message }
                return RepositoryResult.Error(IllegalArgumentException("Validation failed: $errorMessage"))
            }

            // Insert to local database
            val clientId = localClientRepo.insert(client)
            val createdClient = client.copy(id = clientId)

            // Also write to Google Sheets if employee has a sheetName
            val employee = localEmployeeRepo.getById(client.employeeId)
            if (employee != null && employee.sheetName.isNotBlank()) {
                googleSheetsService.addClientToSheet(createdClient, employee.sheetName)
            }

            RepositoryResult.Success(createdClient)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    private fun Client.toClientSimple() = com.payroll.app.desktop.domain.models.ClientSimple(
        id = this.id.toString(),
        name = this.name,
        price = this.price,
        employeePrice = this.employeePrice,
        companyPrice = this.companyPrice,
        employeeId = this.employeeId
    )
}

/**
 * JVM implementation of EmployeeRepository using local database
 */
actual class EmployeeRepository(
    private val localEmployeeRepo: LocalEmployeeRepository,
    private val localClientRepo: LocalClientRepository,
    private val googleSheetsService: GoogleSheetsService
) {
    actual suspend fun getAll(): RepositoryResult<List<Employee>> {
        return try {
            val employees = localEmployeeRepo.getAll()
            RepositoryResult.Success(employees)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun getById(id: String): RepositoryResult<Employee> {
        return try {
            val employee = localEmployeeRepo.getById(id)
            if (employee != null) {
                RepositoryResult.Success(employee)
            } else {
                RepositoryResult.Error(Exception("Employee not found: $id"))
            }
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun createEmployee(employee: Employee): RepositoryResult<Employee> {
        return try {
            // Insert to local database
            localEmployeeRepo.insert(employee)

            // Create Google Sheets tab for the employee if sheetName is specified
            if (employee.sheetName.isNotBlank()) {
                val sheetCreated = googleSheetsService.createEmployeeSheet(employee.sheetName)
                if (!sheetCreated) {
                    Logger.warning("PayrollRepository", "Failed to create sheet tab for employee ${employee.name}")
                    // Don't fail the operation, just log warning
                }
            }

            RepositoryResult.Success(employee)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun updateEmployee(employee: Employee): RepositoryResult<Employee> {
        return try {
            // Update local database first
            localEmployeeRepo.update(employee)

            // Then update Google Sheets "Employees" tab
            when (val sheetsResult = googleSheetsService.updateEmployeeInSheet(employee)) {
                is com.payroll.app.desktop.google.SheetsWriteResult.Success -> {
                    // Successfully updated in sheets
                }
                is com.payroll.app.desktop.google.SheetsWriteResult.Error -> {
                    // Log error but don't fail the update since local DB was updated
                    // User can manually sync later if needed
                    println("⚠️ Warning: Failed to update Google Sheets: ${sheetsResult.message}")
                }
            }

            RepositoryResult.Success(employee)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun deleteEmployee(id: String): RepositoryResult<Boolean> {
        return try {
            localEmployeeRepo.delete(id)
            RepositoryResult.Success(true)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return try {
            val clients = localClientRepo.getByEmployeeId(employeeId)
            RepositoryResult.Success(clients)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }
}