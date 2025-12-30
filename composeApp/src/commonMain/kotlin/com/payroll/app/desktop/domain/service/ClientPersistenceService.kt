package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.models.Employee

/**
 * Service interface for persisting clients
 *
 * STANDALONE DESKTOP APP ARCHITECTURE:
 * - Saves to local SQLite database
 * - Writes to Google Sheets (correct employee tab)
 * - NO backend server calls!
 *
 * Implementation in jvmMain: DesktopClientPersistenceService
 */
interface ClientPersistenceService {

    /**
     * Add a new client and persist to both local DB and Google Sheets
     *
     * @param client The client to add
     * @param employee The employee (to get sheetName for Sheets write)
     * @return Result indicating success or failure
     */
    suspend fun addClient(client: Client, employee: Employee): ClientPersistenceResult

    /**
     * Update an existing client in local DB
     * Note: Google Sheets updates are handled separately
     *
     * @param client The client to update
     * @return Result indicating success or failure
     */
    suspend fun updateClient(client: Client): ClientPersistenceResult

    /**
     * Delete a client from local DB
     *
     * @param clientId The client ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteClient(clientId: Long): ClientPersistenceResult

    /**
     * Get all clients for an employee from local DB
     *
     * @param employeeId The employee ID
     * @return List of clients
     */
    suspend fun getClientsByEmployee(employeeId: String): List<Client>

    /**
     * Check if Google Sheets service is available
     */
    fun isSheetsAvailable(): Boolean
}

/**
 * Result of a client persistence operation
 */
sealed class ClientPersistenceResult {
    data class Success(
        val client: Client,
        val localSaved: Boolean = true,
        val sheetsSaved: Boolean = false,
        val sheetsRange: String? = null
    ) : ClientPersistenceResult()

    data class PartialSuccess(
        val client: Client,
        val localSaved: Boolean,
        val sheetsSaved: Boolean,
        val message: String
    ) : ClientPersistenceResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ClientPersistenceResult()
}
