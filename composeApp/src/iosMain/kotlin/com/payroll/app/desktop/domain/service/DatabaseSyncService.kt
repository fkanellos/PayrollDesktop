package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.domain.models.SyncDatabaseResponse

/**
 * iOS stub for DatabaseSyncService
 */
actual class DatabaseSyncService {
    actual suspend fun syncFromSheets(): Result<SyncDatabaseResponse> {
        return Result.failure(Exception("Database sync is not supported on iOS"))
    }
}