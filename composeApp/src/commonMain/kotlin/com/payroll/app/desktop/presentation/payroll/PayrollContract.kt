package com.payroll.app.desktop.presentation.payroll

import com.payroll.app.desktop.core.base.UiAction
import com.payroll.app.desktop.core.base.UiEffect
import com.payroll.app.desktop.core.base.UiState
import com.payroll.app.desktop.domain.models.*
import kotlinx.datetime.LocalDateTime

/**
 * PayrollState - All possible states for the payroll screen
 */
data class PayrollState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val employees: List<Employee> = emptyList(),
    val selectedEmployee: Employee? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val payrollResult: PayrollResponse? = null,
    val isCalculating: Boolean = false,
    val showDatePicker: Boolean = false,
    val datePickerType: DatePickerType = DatePickerType.START,
    // Enhanced fields
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    // Sync Database fields
    val isSyncing: Boolean = false,
    val syncResult: SyncDatabaseResult? = null,
    // Added unmatched clients (names added during this session)
    val addedClients: Set<String> = emptySet(),
    // Uncertain matches needing user confirmation
    val uncertainMatches: List<UncertainMatch> = emptyList()
) : UiState

/**
 * Sync database result
 */
data class SyncDatabaseResult(
    val success: Boolean,
    val employeesInserted: Int = 0,
    val employeesUpdated: Int = 0,
    val clientsInserted: Int = 0,
    val clientsUpdated: Int = 0,
    val durationMs: Long = 0,
    val errorMessage: String? = null
)

enum class DatePickerType {
    START, END
}

/**
 * PayrollAction - All user actions that can be performed
 */
sealed class PayrollAction : UiAction {
    // Loading actions
    object LoadEmployees : PayrollAction()
    object RefreshData : PayrollAction()

    // Employee selection
    data class SelectEmployee(val employee: Employee) : PayrollAction()

    // Date selection - Enhanced με calendar picker support
    data class SetStartDate(val date: LocalDateTime) : PayrollAction()
    data class SetEndDate(val date: LocalDateTime) : PayrollAction()
    data class ShowDatePicker(val type: DatePickerType) : PayrollAction()
    object HideDatePicker : PayrollAction()
    object SetDefaultDateRange : PayrollAction()

    // New calendar picker actions
    object ShowStartDatePicker : PayrollAction()
    object ShowEndDatePicker : PayrollAction()
    object HideStartDatePicker : PayrollAction()
    object HideEndDatePicker : PayrollAction()

    // Payroll calculation
    object CalculatePayroll : PayrollAction()
    object ClearResults : PayrollAction()

    // Export actions
    object ExportToPdf : PayrollAction()
    object ExportToExcel : PayrollAction()

    // Sync Database actions
    object SyncDatabase : PayrollAction()
    object ClearSyncResult : PayrollAction()

    // Add unmatched client to database
    data class AddUnmatchedClient(
        val originalEventTitle: String,  // Original event title for tracking
        val name: String,                 // Edited client name
        val price: Double = 50.0,
        val employeePrice: Double = 22.5,
        val companyPrice: Double = 27.5
    ) : PayrollAction()

    // Match confirmation actions
    data class ConfirmMatch(val match: UncertainMatch) : PayrollAction()
    data class RejectMatch(val match: UncertainMatch) : PayrollAction()
    object ClearUncertainMatches : PayrollAction()

    // Error handling
    object ClearError : PayrollAction()
}

/**
 * PayrollEffect - Side effects that don't change state
 */
sealed class PayrollEffect : UiEffect {
    // Navigation
    object NavigateToAdmin : PayrollEffect()
    object NavigateToEmployeeManagement : PayrollEffect()

    // User feedback
    data class ShowToast(val message: String) : PayrollEffect()
    data class ShowError(val error: String) : PayrollEffect()

    // File operations
    data class SavePdfFile(val filename: String, val data: ByteArray) : PayrollEffect()
    data class SaveExcelFile(val filename: String, val data: ByteArray) : PayrollEffect()

    /**
     * 🆕 Request confirmation για Google Sheets sync
     */
    data class RequestSheetsConfirmation(
        val payrollId: String,
        val message: String,
        val isUpdate: Boolean
    ) : PayrollEffect()

    // System operations
    data class OpenUrl(val url: String) : PayrollEffect()
    object RequestCalendarPermission : PayrollEffect()

    // Sync Database effects
    data class SyncDatabaseComplete(val result: SyncDatabaseResult) : PayrollEffect()

    // Client added effects
    data class ClientAdded(val clientName: String) : PayrollEffect()
    data class ClientAddFailed(val clientName: String, val error: String) : PayrollEffect()
}

/**
 * UI Data Classes for easier handling
 */
data class PayrollFormData(
    val selectedEmployee: Employee?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val isValid: Boolean = false
) {
    companion object {
        fun empty() = PayrollFormData(
            selectedEmployee = null,
            startDate = null,
            endDate = null,
            isValid = false
        )

        fun PayrollState.toFormData() = PayrollFormData(
            selectedEmployee = selectedEmployee,
            startDate = startDate,
            endDate = endDate,
            isValid = selectedEmployee != null && startDate != null && endDate != null
        )
    }
}

/**
 * Result states for better UI handling
 */
sealed class PayrollCalculationState {
    object Idle : PayrollCalculationState()
    object Loading : PayrollCalculationState()
    data class Success(val result: PayrollResponse) : PayrollCalculationState()
    data class Error(val message: String) : PayrollCalculationState()
}