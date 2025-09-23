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
    val datePickerType: DatePickerType = DatePickerType.START
) : UiState

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

    // Date selection
    data class SetStartDate(val date: LocalDateTime) : PayrollAction()
    data class SetEndDate(val date: LocalDateTime) : PayrollAction()
    data class ShowDatePicker(val type: DatePickerType) : PayrollAction()
    object HideDatePicker : PayrollAction()
    object SetDefaultDateRange : PayrollAction()

    // Payroll calculation
    object CalculatePayroll : PayrollAction()
    object ClearResults : PayrollAction()

    // Export actions
    object ExportToPdf : PayrollAction()
    object ExportToExcel : PayrollAction()

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

    // System operations
    data class OpenUrl(val url: String) : PayrollEffect()
    object RequestCalendarPermission : PayrollEffect()
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