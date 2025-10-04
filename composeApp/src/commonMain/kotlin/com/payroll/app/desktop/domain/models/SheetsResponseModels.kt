package com.payroll.app.desktop.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 🆕 Response από /payroll/{id}/check-sheets
 */
@Serializable
data class CheckSheetsResponse(
    val exists: Boolean,
    val employeeName: String,
    val period: String,
    val existingMasterRow: Int? = null,
    val existingDetailRows: Int,
    val action: String,  // "update" | "insert"
    val message: String
)

/**
 * 🆕 Response από /payroll/{id}/sync-to-sheets
 */
@Serializable
data class SyncSheetsResponse(
    val status: String,  // "success" | "error"
    val message: String? = null,
    val mode: String? = null,  // "updated" | "inserted"
    val employeeName: String? = null,
    val period: String? = null,
    val totalSessions: Int? = null,
    val totalRevenue: Double? = null,
    val masterWritten: Boolean? = null,
    val detailsWritten: Boolean? = null,
    val masterRows: Int? = null,
    val detailRows: Int? = null,
    // Error fields
    val errorType: String? = null,
    val errorDetails: String? = null
)