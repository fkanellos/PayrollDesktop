package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.data.repositories.LocalClientRepository
import com.payroll.app.desktop.data.repositories.LocalEmployeeRepository
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.domain.models.SyncDatabaseResponse
import com.payroll.app.desktop.google.GoogleSheetsService
import com.payroll.app.desktop.google.SheetsReadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for syncing data from Google Sheets to local database
 */
actual class DatabaseSyncService(
    private val sheetsService: GoogleSheetsService,
    private val employeeRepository: LocalEmployeeRepository,
    private val clientRepository: LocalClientRepository
) {
    companion object {
        private const val TAG = "DatabaseSyncService"
    }

    /**
     * Sync all employees and clients from Google Sheets to local database
     */
    actual suspend fun syncFromSheets(): Result<SyncDatabaseResponse> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()

            Logger.info(TAG, "Starting database sync from Google Sheets...")

            // Step 1: Read employees from the "Employees" sheet
            val employeesResult = sheetsService.readEmployees()

            when (employeesResult) {
                is SheetsReadResult.Error -> {
                    Logger.error(TAG, "Failed to read employees: ${employeesResult.message}")
                    return@withContext Result.failure(Exception(employeesResult.message))
                }
                is SheetsReadResult.Success -> {
                    val employeesData = employeesResult.data
                    Logger.info(TAG, "Found ${employeesData.size} employees in sheet")

                    var employeesInserted = 0
                    var employeesUpdated = 0
                    var clientsInserted = 0
                    var clientsUpdated = 0

                    // Step 2: Process each employee
                    for (empData in employeesData) {
                        // Generate employee ID from email
                        val employeeId = empData.calendarId.ifBlank { empData.email }

                        // Check if employee exists
                        val existingEmployee = employeeRepository.getById(employeeId)

                        if (existingEmployee == null) {
                            // Insert new employee
                            val newEmployee = Employee(
                                id = employeeId,
                                name = empData.name,
                                email = empData.email,
                                calendarId = empData.calendarId,
                                color = generateColorForEmployee(empData.name),
                                sheetName = empData.sheetName,
                                supervisionPrice = empData.supervisionPrice
                            )
                            employeeRepository.insert(newEmployee)
                            employeesInserted++
                            Logger.info(TAG, "Inserted employee: ${empData.name}")
                        } else {
                            // Update existing employee
                            val updatedEmployee = existingEmployee.copy(
                                name = empData.name,
                                email = empData.email,
                                calendarId = empData.calendarId,
                                sheetName = empData.sheetName,
                                supervisionPrice = empData.supervisionPrice
                            )
                            employeeRepository.update(updatedEmployee)
                            employeesUpdated++
                            Logger.info(TAG, "Updated employee: ${empData.name}")
                        }

                        // Step 3: Read and sync clients for this employee
                        if (empData.sheetName.isNotBlank()) {
                            val clientsResult = sheetsService.readClientsForEmployee(
                                sheetName = empData.sheetName,
                                employeeId = employeeId
                            )

                            when (clientsResult) {
                                is SheetsReadResult.Error -> {
                                    Logger.warning(TAG, "Failed to read clients for ${empData.name}: ${clientsResult.message}")
                                }
                                is SheetsReadResult.Success -> {
                                    val clientsData = clientsResult.data
                                    Logger.info(TAG, "Found ${clientsData.size} clients for ${empData.name}")

                                    for (clientData in clientsData) {
                                        // Check if client exists for this employee
                                        val existingClient = clientRepository.getByEmployeeIdAndName(
                                            employeeId = employeeId,
                                            name = clientData.name
                                        )

                                        if (existingClient == null) {
                                            // Insert new client
                                            val newClient = Client(
                                                id = 0, // Auto-generated
                                                name = clientData.name,
                                                price = clientData.price,
                                                employeePrice = clientData.employeePrice,
                                                companyPrice = clientData.companyPrice,
                                                employeeId = employeeId,
                                                pendingPayment = false
                                            )
                                            clientRepository.insert(newClient)
                                            clientsInserted++
                                            Logger.debug(TAG, "Inserted client: ${clientData.name}")
                                        } else {
                                            // Update existing client
                                            val updatedClient = existingClient.copy(
                                                price = clientData.price,
                                                employeePrice = clientData.employeePrice,
                                                companyPrice = clientData.companyPrice
                                            )
                                            clientRepository.update(updatedClient)
                                            clientsUpdated++
                                            Logger.debug(TAG, "Updated client: ${clientData.name}")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val durationMs = System.currentTimeMillis() - startTime
                    val response = SyncDatabaseResponse(
                        employeesInserted = employeesInserted,
                        employeesUpdated = employeesUpdated,
                        clientsInserted = clientsInserted,
                        clientsUpdated = clientsUpdated,
                        durationMs = durationMs
                    )

                    Logger.info(TAG, "Sync completed in ${durationMs}ms")
                    Logger.info(TAG, "Employees: +$employeesInserted / ↻$employeesUpdated")
                    Logger.info(TAG, "Clients: +$clientsInserted / ↻$clientsUpdated")

                    Result.success(response)
                }
            }
        } catch (e: Exception) {
            Logger.error(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }

    /**
     * Generate a color for an employee based on their name
     */
    private fun generateColorForEmployee(name: String): String {
        val colors = listOf(
            "#FF6B6B", // Red
            "#4ECDC4", // Teal
            "#45B7D1", // Blue
            "#FFA07A", // Light Salmon
            "#98D8C8", // Mint
            "#F7DC6F", // Yellow
            "#BB8FCE", // Purple
            "#85C1E2", // Light Blue
            "#F8B739", // Orange
            "#52B788"  // Green
        )

        // Use name hashcode to consistently assign the same color to the same name
        val index = Math.abs(name.hashCode()) % colors.size
        return colors[index]
    }
}