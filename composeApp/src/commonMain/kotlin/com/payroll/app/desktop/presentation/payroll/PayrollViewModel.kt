package com.payroll.app.desktop.presentation.payroll

import com.payroll.app.desktop.core.base.BaseViewModel
import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.models.ClientPayrollDetail
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.domain.models.EmployeeInfo
import com.payroll.app.desktop.domain.models.PayrollRequest
import com.payroll.app.desktop.domain.models.PayrollResponse
import com.payroll.app.desktop.domain.models.PayrollSummary
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus

/**
 * PayrollViewModel implementing MVI pattern
 * Handles all payroll calculation logic and state management
 */
class PayrollViewModel(
    private val payrollRepository: PayrollRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) : BaseViewModel<PayrollState, PayrollAction, PayrollEffect>() {

    override val initialState = PayrollState()

    private val _sideEffect = MutableSharedFlow<PayrollEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        // Initialize state first
        _uiState.value = initialState

        // Then load data
        scope.launch {
            delay(100) // Small delay to ensure UI is ready
            handleAction(PayrollAction.LoadEmployees)
            handleAction(PayrollAction.SetDefaultDateRange)
        }
    }

    override fun reduce(currentState: PayrollState, action: PayrollAction): PayrollState {
        return when (action) {
            // Loading Actions
            PayrollAction.LoadEmployees -> {
                loadEmployees()
                currentState.copy(isLoading = true, error = null)
            }

            PayrollAction.RefreshData -> {
                loadEmployees()
                currentState.copy(isLoading = true, error = null)
            }

            // Employee Selection
            is PayrollAction.SelectEmployee -> {
                currentState.copy(
                    selectedEmployee = action.employee,
                    error = null
                )
            }

            // Date Selection
            is PayrollAction.SetStartDate -> {
                val newState = currentState.copy(startDate = action.date, error = null)
                validateDateRange(newState)
            }

            is PayrollAction.SetEndDate -> {
                val newState = currentState.copy(endDate = action.date, error = null)
                validateDateRange(newState)
            }

            is PayrollAction.ShowDatePicker -> {
                currentState.copy(showDatePicker = true, datePickerType = action.type)
            }

            PayrollAction.HideDatePicker -> {
                currentState.copy(showDatePicker = false)
            }

            PayrollAction.SetDefaultDateRange -> {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val twoWeeksAgo = now.date.minus(kotlinx.datetime.DatePeriod(days = 14))
                val startDateTime = LocalDateTime(twoWeeksAgo, now.time)

                currentState.copy(
                    startDate = startDateTime,
                    endDate = now,
                    error = null
                )
            }

            // Payroll Calculation
            PayrollAction.CalculatePayroll -> {
                if (canCalculatePayroll(currentState)) {
                    calculatePayroll(currentState)
                    currentState.copy(isCalculating = true, error = null)
                } else {
                    emitSideEffect(PayrollEffect.ShowError("Παρακαλώ συμπληρώστε όλα τα απαραίτητα πεδία"))
                    currentState
                }
            }

            PayrollAction.ClearResults -> {
                currentState.copy(payrollResult = null)
            }

            // Export Actions
            PayrollAction.ExportToPdf -> {
                currentState.payrollResult?.let { result ->
                    exportToPdf(result)
                } ?: run {
                    emitSideEffect(PayrollEffect.ShowError("Δεν υπάρχουν αποτελέσματα για εξαγωγή"))
                }
                currentState
            }

            PayrollAction.ExportToExcel -> {
                currentState.payrollResult?.let { result ->
                    exportToExcel(result)
                } ?: run {
                    emitSideEffect(PayrollEffect.ShowError("Δεν υπάρχουν αποτελέσματα για εξαγωγή"))
                }
                currentState
            }

            // Error Handling
            PayrollAction.ClearError -> {
                currentState.copy(error = null)
            }
        }
    }

    private fun loadEmployees() {
        scope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }

                // Use real API instead of mock
                when (val result = payrollRepository.getAllEmployees()) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            currentState.copy(
                                employees = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = "Σφάλμα φόρτωσης εργαζομένων: ${result.exception.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Σφάλμα φόρτωσης εργαζομένων"
                    )
                }
            }
        }
    }

    private fun validateDateRange(state: PayrollState): PayrollState {
        val startDate = state.startDate
        val endDate = state.endDate

        return if (startDate != null && endDate != null && startDate >= endDate) {
            state.copy(error = "Η ημερομηνία έναρξης πρέπει να είναι πριν τη λήξη")
        } else {
            state.copy(error = null)
        }
    }

    private fun canCalculatePayroll(state: PayrollState): Boolean {
        return state.selectedEmployee != null &&
                state.startDate != null &&
                state.endDate != null &&
                state.error == null
    }

    private fun calculatePayroll(state: PayrollState) {
        val employee = state.selectedEmployee ?: return
        val startDate = state.startDate ?: return
        val endDate = state.endDate ?: return

        scope.launch {
            try {
                // TODO: Replace with actual API call
                delay(2000) // Simulate API call

                val request = PayrollRequest(
                    employeeId = employee.id,
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )

                // Mock response - replace with actual API call
                val mockResult = createMockPayrollResponse(employee, startDate, endDate)

                updateState { currentState ->
                    currentState.copy(
                        payrollResult = mockResult,
                        isCalculating = false
                    )
                }

                emitSideEffect(PayrollEffect.ShowToast("Υπολογισμός μισθοδοσίας ολοκληρώθηκε επιτυχώς"))

            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isCalculating = false,
                        error = e.message ?: "Σφάλμα υπολογισμού μισθοδοσίας"
                    )
                }
                emitSideEffect(PayrollEffect.ShowError("Σφάλμα υπολογισμού: ${e.message}"))
            }
        }
    }

    private fun exportToPdf(result: PayrollResponse) {
        scope.launch {
            try {
                // TODO: Implement actual PDF generation
                val filename = "payroll_${result.employee.name}_${Clock.System.now()}.pdf"
                emitSideEffect(PayrollEffect.ShowToast("Εξαγωγή PDF: $filename"))
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError("Σφάλμα εξαγωγής PDF: ${e.message}"))
            }
        }
    }

    private fun exportToExcel(result: PayrollResponse) {
        scope.launch {
            try {
                // TODO: Implement actual Excel generation
                val filename = "payroll_${result.employee.name}_${Clock.System.now()}.xlsx"
                emitSideEffect(PayrollEffect.ShowToast("Εξαγωγή Excel: $filename"))
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError("Σφάλμα εξαγωγής Excel: ${e.message}"))
            }
        }
    }

    private fun emitSideEffect(effect: PayrollEffect) {
        scope.launch {
            _sideEffect.emit(effect)
        }
    }


    // Mock data for testing
    private fun createMockPayrollResponse(employee: Employee, startDate: LocalDateTime, endDate: LocalDateTime) =
        PayrollResponse(
            employee = EmployeeInfo(
                id = employee.id,
                name = employee.name,
                email = employee.email
            ),
            period = "${startDate.date} - ${endDate.date}",
            summary = PayrollSummary(
                totalSessions = 42,
                totalRevenue = 2080.0,
                employeeEarnings = 832.0,
                companyEarnings = 1248.0
            ),
            clientBreakdown = listOf(
                ClientPayrollDetail(
                    clientName = "Κωνσταντίνος Κουρμούζης",
                    pricePerSession = 50.0,
                    employeePricePerSession = 20.0,
                    companyPricePerSession = 30.0,
                    sessions = 4,
                    totalRevenue = 200.0,
                    employeeEarnings = 80.0,
                    companyEarnings = 120.0
                )
            ),
            generatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                .toString()
        )

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}