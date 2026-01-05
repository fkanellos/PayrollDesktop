package com.payroll.app.desktop.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.payroll.app.desktop.MatchConfirmationEntity
import com.payroll.app.desktop.core.utils.StringNormalizer
import com.payroll.app.desktop.database.PayrollDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Repository for managing match confirmations
 * Stores user decisions about uncertain client name matches
 */
class MatchConfirmationRepository(
    private val database: PayrollDatabase
) : com.payroll.app.desktop.presentation.payroll.IMatchConfirmationRepository {
    private val queries = database.payrollDatabaseQueries

    /**
     * Check if a match confirmation exists for this event title and employee
     *
     * @param eventTitle The original event title (will be normalized internally)
     * @param employeeId The employee ID
     * @return The confirmed client name, or null if no confirmation exists
     */
    override suspend fun getConfirmedMatch(
        eventTitle: String,
        employeeId: String
    ): String? = withContext(Dispatchers.IO) {
        val normalized = StringNormalizer.normalize(eventTitle)

        queries.selectMatchConfirmation(normalized, employeeId)
            .executeAsOneOrNull()
            ?.matched_client_name
    }

    /**
     * Save a match confirmation
     *
     * @param eventTitle The original event title (will be normalized internally)
     * @param matchedClientName The client name that was confirmed
     * @param employeeId The employee ID
     */
    override suspend fun saveConfirmation(
        eventTitle: String,
        matchedClientName: String,
        employeeId: String
    ) = withContext(Dispatchers.IO) {
        val normalized = StringNormalizer.normalize(eventTitle)
        val timestamp = Clock.System.now().toEpochMilliseconds()

        queries.insertMatchConfirmation(
            event_title_normalized = normalized,
            matched_client_name = matchedClientName,
            employee_id = employeeId,
            created_at = timestamp
        )
    }

    /**
     * Get all confirmations for an employee
     */
    suspend fun getConfirmationsByEmployee(employeeId: String) = withContext(Dispatchers.IO) {
        queries.selectMatchConfirmationsByEmployee(employeeId)
            .executeAsList()
    }

    /**
     * Delete a specific confirmation
     */
    suspend fun deleteConfirmation(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteMatchConfirmation(id)
    }

    /**
     * Delete all confirmations for an employee
     */
    suspend fun deleteConfirmationsByEmployee(employeeId: String) = withContext(Dispatchers.IO) {
        queries.deleteMatchConfirmationsByEmployee(employeeId)
    }

    /**
     * Get all confirmations as a Flow (for UI observation)
     */
    fun getAllConfirmationsFlow(): Flow<List<MatchConfirmationEntity>> {
        return queries.selectAllMatchConfirmations()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    /**
     * Clear all confirmations (useful for testing or reset)
     */
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        queries.deleteAllMatchConfirmations()
    }
}
