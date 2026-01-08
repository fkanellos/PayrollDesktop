package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.domain.models.PushToSheetsResponse
import com.payroll.app.desktop.domain.models.SyncDatabaseResponse

/**
 * Platform-specific service for syncing database from external sources
 */
expect class DatabaseSyncService {
    suspend fun syncFromSheets(): Result<SyncDatabaseResponse>
    suspend fun pushToSheets(): Result<PushToSheetsResponse>
}