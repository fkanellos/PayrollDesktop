package com.payroll.app.desktop.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.payroll.app.desktop.MatchConfirmationEntity
import com.payroll.app.desktop.core.utils.StringNormalizer
import com.payroll.app.desktop.database.PayrollDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Repository for managing match confirmations
 * Stores user decisions about uncertain client name matches
 *
 * 🔥 HIGH FIX: Uses Mutex to prevent race conditions on rapid user confirmations
 */
class MatchConfirmationRepository(
    private val database: PayrollDatabase
) : com.payroll.app.desktop.presentation.payroll.IMatchConfirmationRepository {
    private val queries = database.payrollDatabaseQueries

    // 🔥 Mutex to prevent race conditions when saving confirmations
    private val saveMutex = Mutex()

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
     *
     * 🔥 HIGH FIX: Protected by Mutex to prevent race conditions
     * If user rapidly clicks confirm buttons, this ensures saves are serialized
     */
    override suspend fun saveConfirmation(
        eventTitle: String,
        matchedClientName: String,
        employeeId: String
    ) = withContext(Dispatchers.IO) {
        // 🔥 Acquire mutex lock to prevent concurrent saves
        saveMutex.withLock {
            val normalized = StringNormalizer.normalize(eventTitle)
            val timestamp = Clock.System.now().toEpochMilliseconds()

            queries.insertMatchConfirmation(
                event_title_normalized = normalized,
                matched_client_name = matchedClientName,
                employee_id = employeeId,
                created_at = timestamp
            )
        }
    }

    /**
     * Get all confirmations for an employee
     */
    suspend fun getConfirmationsByEmployee(employeeId: String) = withContext(Dispatchers.IO) {
        queries.selectMatchConfirmationsByEmployee(employeeId)
            .executeAsList()
    }

    /**
     * 🔥 HIGH FIX: Batch load all confirmed matches for an employee as a Map
     * Returns Map<EventTitleNormalized, MatchedClientName>
     *
     * This avoids N+1 query problem when filtering uncertain matches:
     * - OLD: 50 uncertain matches = 50 queries
     * - NEW: 50 uncertain matches = 1 batch query + in-memory lookup
     */
    override suspend fun getAllConfirmedMatchesMap(employeeId: String): Map<String, String> = withContext(Dispatchers.IO) {
        queries.selectMatchConfirmationsByEmployee(employeeId)
            .executeAsList()
            .associate { it.event_title_normalized to it.matched_client_name }
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
