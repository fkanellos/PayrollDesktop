package com.payroll.app.desktop.service

import com.payroll.app.desktop.data.repositories.LocalClientRepository
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.domain.service.ClientPersistenceResult
import com.payroll.app.desktop.domain.service.ClientPersistenceService
import com.payroll.app.desktop.google.GoogleSheetsService
import com.payroll.app.desktop.google.SheetsWriteResult

/**
 * Desktop implementation of ClientPersistenceService
 *
 * STANDALONE DESKTOP APP:
 * - Saves clients to local SQLite database
 * - Writes clients to Google Sheets (correct employee tab)
 * - NO backend server!
 *
 * IMPORTANT:
 * - Google Calendar = READ ONLY (handled by GoogleCalendarRepository)
 * - Google Sheets = WRITE allowed (this service handles it)
 */
class DesktopClientPersistenceService(
    private val localClientRepository: LocalClientRepository,
    private val googleSheetsService: GoogleSheetsService
) : ClientPersistenceService {

    override suspend fun addClient(client: Client, employee: Employee): ClientPersistenceResult {
        println("📝 DesktopClientPersistenceService.addClient:")
        println("   Client: ${client.name}")
        println("   Employee: ${employee.name}")
        println("   Sheet Tab: ${employee.sheetName}")

        // Step 1: Save to local SQLite database
        val savedClient: Client
        try {
            val localId = localClientRepository.insert(client)
            savedClient = client.copy(id = localId)
            println("✅ Saved to local SQLite with ID: $localId")
        } catch (e: Exception) {
            println("❌ Failed to save to local SQLite: ${e.message}")
            return ClientPersistenceResult.Error(
                message = "Failed to save client to local database: ${e.message}",
                cause = e
            )
        }

        // Step 2: Write to Google Sheets (employee's tab)
        val sheetName = employee.sheetName
        if (sheetName.isBlank()) {
            println("⚠️ Employee has no sheetName configured, skipping Sheets write")
            return ClientPersistenceResult.PartialSuccess(
                client = savedClient,
                localSaved = true,
                sheetsSaved = false,
                message = "Saved to local DB, but employee has no sheet tab configured"
            )
        }

        if (!googleSheetsService.isAvailable()) {
            println("⚠️ Google Sheets service not available, skipping Sheets write")
            return ClientPersistenceResult.PartialSuccess(
                client = savedClient,
                localSaved = true,
                sheetsSaved = false,
                message = "Saved to local DB, but Google Sheets not available"
            )
        }

        return when (val sheetsResult = googleSheetsService.addClientToSheet(savedClient, sheetName)) {
            is SheetsWriteResult.Success -> {
                println("✅ Wrote to Google Sheets: ${sheetsResult.updatedRange}")
                ClientPersistenceResult.Success(
                    client = savedClient,
                    localSaved = true,
                    sheetsSaved = true,
                    sheetsRange = sheetsResult.updatedRange
                )
            }
            is SheetsWriteResult.Error -> {
                println("⚠️ Failed to write to Sheets (client saved locally): ${sheetsResult.message}")
                ClientPersistenceResult.PartialSuccess(
                    client = savedClient,
                    localSaved = true,
                    sheetsSaved = false,
                    message = "Saved to local DB, but Sheets write failed: ${sheetsResult.message}"
                )
            }
        }
    }

    override suspend fun updateClient(client: Client): ClientPersistenceResult {
        return try {
            localClientRepository.update(client)
            println("✅ Updated client in local SQLite: ${client.name}")
            ClientPersistenceResult.Success(
                client = client,
                localSaved = true,
                sheetsSaved = false // Note: Sheets updates handled separately
            )
        } catch (e: Exception) {
            println("❌ Failed to update client: ${e.message}")
            ClientPersistenceResult.Error(
                message = "Failed to update client: ${e.message}",
                cause = e
            )
        }
    }

    override suspend fun deleteClient(clientId: Long): ClientPersistenceResult {
        return try {
            val existingClient = localClientRepository.getById(clientId)
            if (existingClient != null) {
                localClientRepository.delete(clientId)
                println("✅ Deleted client from local SQLite: ID $clientId")
                ClientPersistenceResult.Success(
                    client = existingClient,
                    localSaved = true,
                    sheetsSaved = false
                )
            } else {
                ClientPersistenceResult.Error(message = "Client not found with ID: $clientId")
            }
        } catch (e: Exception) {
            println("❌ Failed to delete client: ${e.message}")
            ClientPersistenceResult.Error(
                message = "Failed to delete client: ${e.message}",
                cause = e
            )
        }
    }

    override suspend fun getClientsByEmployee(employeeId: String): List<Client> {
        return try {
            localClientRepository.getByEmployeeId(employeeId)
        } catch (e: Exception) {
            println("❌ Failed to get clients: ${e.message}")
            emptyList()
        }
    }

    override fun isSheetsAvailable(): Boolean = googleSheetsService.isAvailable()
}
