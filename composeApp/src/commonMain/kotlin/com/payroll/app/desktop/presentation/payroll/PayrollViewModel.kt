package com.payroll.app.desktop.presentation.payroll

import com.payroll.app.desktop.core.base.BaseViewModel
import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.core.export.ExportResult
import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.format
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.models.ClientPayrollDetail
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.domain.models.EmployeeInfo
import com.payroll.app.desktop.domain.models.PayrollResponse
import com.payroll.app.desktop.domain.models.PayrollSummary
import com.payroll.app.desktop.domain.models.UncertainMatch
import com.payroll.app.desktop.domain.service.DatabaseSyncService
import com.payroll.app.desktop.domain.usecases.ClientQuickAddUseCase
import com.payroll.app.desktop.domain.usecases.MatchConfirmationUseCase
import com.payroll.app.desktop.domain.usecases.PayrollCalculationUseCase
import com.payroll.app.desktop.domain.usecases.PayrollExportUseCase
import com.payroll.app.desktop.utils.DateRanges
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
 * Handles UI state management and delegates business logic to UseCases
 * Enhanced με calendar support και improved UX
 */
// Interface for MatchConfirmationRepository (to avoid platform-specific dependencies)
interface IMatchConfirmationRepository {
    suspend fun saveConfirmation(eventTitle: String, matchedClientName: String, employeeId: String)
    suspend fun getConfirmedMatch(eventTitle: String, employeeId: String): String?

    /**
     * 🔥 HIGH FIX: Batch load all confirmed matches for an employee as a Map
     * Returns Map<EventTitleNormalized, MatchedClientName>
     *
     * This avoids N+1 query problem when filtering uncertain matches:
     * - OLD: 50 uncertain matches = 50 queries
     * - NEW: 50 uncertain matches = 1 batch query + in-memory lookup
     */
    suspend fun getAllConfirmedMatchesMap(employeeId: String): Map<String, String>
}

class PayrollViewModel(
    private val payrollRepository: PayrollRepository,
    private val databaseSyncService: DatabaseSyncService,
    private val payrollCalculationUseCase: PayrollCalculationUseCase,
    private val matchConfirmationUseCase: MatchConfirmationUseCase,
    private val clientQuickAddUseCase: ClientQuickAddUseCase,
    private val payrollExportUseCase: PayrollExportUseCase
) : BaseViewModel<PayrollState, PayrollAction, PayrollEffect>() {

    override val initialState = PayrollState()

    private val _sideEffect = MutableSharedFlow<PayrollEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        // Initialize state first
        _uiState.value = initialState

        // Then load data
        viewModelScope.launch {
            delay(AppConstants.Timing.UI_INIT_DELAY_MS)
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
                Logger.debug("PayrollViewModel", "PayrollAction.RefreshData received")
                refreshData()
                currentState.copy(isLoading = true, error = null)
            }

            // Employee Selection - Enhanced με validation
            is PayrollAction.SelectEmployee -> {
                // Clear previous results when changing employee
                val newState = currentState.copy(
                    selectedEmployee = action.employee,
                    error = null,
                    payrollResult = null
                )

                // Validate employee and show info
                validateSelectedEmployee(action.employee)
                newState
            }

            // Date Selection - Enhanced με calendar picker support
            is PayrollAction.SetStartDate -> {
                val newState = currentState.copy(startDate = action.date, error = null)
                validateDateRange(newState)
            }

            is PayrollAction.SetEndDate -> {
                val newState = currentState.copy(endDate = action.date, error = null)
                validateDateRange(newState)
            }

            // Calendar Date Picker Actions - Enhanced
            is PayrollAction.ShowDatePicker -> {
                currentState.copy(showDatePicker = true, datePickerType = action.type)
            }

            PayrollAction.HideDatePicker -> {
                currentState.copy(showDatePicker = false)
            }

            PayrollAction.ShowStartDatePicker -> {
                currentState.copy(showStartDatePicker = true)
            }

            PayrollAction.ShowEndDatePicker -> {
                currentState.copy(showEndDatePicker = true)
            }

            PayrollAction.HideStartDatePicker -> {
                currentState.copy(showStartDatePicker = false)
            }

            PayrollAction.HideEndDatePicker -> {
                currentState.copy(showEndDatePicker = false)
            }

            PayrollAction.SetDefaultDateRange -> {
                val (startDateTime, endDateTime) = DateRanges.twoWorkWeeks()

                currentState.copy(
                    startDate = startDateTime,
                    endDate = endDateTime,
                    error = null
                )
            }

            // Payroll Calculation - Enhanced με better validation
            PayrollAction.CalculatePayroll -> {
                if (canCalculatePayroll(currentState)) {
                    calculatePayroll(currentState)
                    currentState.copy(isCalculating = true, error = null)
                } else {
                    val errorMessage = getValidationErrorMessage(currentState)
                    emitSideEffect(PayrollEffect.ShowError(errorMessage))
                    currentState.copy(error = errorMessage)
                }
            }

            PayrollAction.ClearResults -> {
                currentState.copy(payrollResult = null)
            }

            // Export Actions - Enhanced με better feedback
            PayrollAction.ExportToPdf -> {
                currentState.payrollResult?.let { result ->
                    exportToPdf(result)
                    emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.creatingPdf))
                } ?: run {
                    emitSideEffect(PayrollEffect.ShowError(Strings.Payroll.noResultsToExport))
                }
                currentState
            }

            PayrollAction.ExportToExcel -> {
                currentState.payrollResult?.let { result ->
                    exportToExcel(result)
                    emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.creatingExcel))
                } ?: run {
                    emitSideEffect(PayrollEffect.ShowError(Strings.Payroll.noResultsToExport))
                }
                currentState
            }

            // Sync Database Actions
            PayrollAction.SyncDatabase -> {
                syncDatabase()
                currentState.copy(isSyncing = true, syncResult = null, error = null)
            }

            PayrollAction.ClearSyncResult -> {
                currentState.copy(syncResult = null)
            }

            // Add unmatched client to database
            is PayrollAction.AddUnmatchedClient -> {
                addUnmatchedClient(
                    name = action.name,
                    price = action.price,
                    employeePrice = action.employeePrice,
                    companyPrice = action.companyPrice,
                    employeeId = currentState.selectedEmployee?.id
                )
                // Optimistically add original event title to addedClients set (for UI filtering)
                currentState.copy(
                    addedClients = currentState.addedClients + action.originalEventTitle
                )
            }

            // Match Confirmation Actions
            is PayrollAction.ConfirmMatch -> {
                confirmMatch(action.match, currentState.selectedEmployee?.id)
                currentState
            }

            is PayrollAction.RejectMatch -> {
                rejectMatch(action.match)
                currentState
            }

            PayrollAction.ClearUncertainMatches -> {
                currentState.copy(uncertainMatches = emptyList())
            }

            // Error Handling
            PayrollAction.ClearError -> {
                currentState.copy(error = null)
            }

            PayrollAction.DismissErrorDialog -> {
                currentState.copy(showErrorDialog = false, errorDialogMessage = null)
            }
        }
    }

    /**
     * Confirm an uncertain match - delegates to MatchConfirmationUseCase
     */
    private fun confirmMatch(match: UncertainMatch, employeeId: String?) {
        if (employeeId == null) {
            emitSideEffect(PayrollEffect.ShowError("No employee selected"))
            return
        }

        viewModelScope.launch {
            when (val result = matchConfirmationUseCase.confirmMatch(match, employeeId)) {
                is MatchConfirmationUseCase.ConfirmationResult.Confirmed -> {
                    // Remove from uncertainMatches list
                    updateState { currentState ->
                        currentState.copy(
                            uncertainMatches = currentState.uncertainMatches - match
                        )
                    }

                    emitSideEffect(
                        PayrollEffect.ShowToast(
                            Strings.Payroll.matchConfirmed.format(result.eventTitle, result.clientName)
                        )
                    )

                    // If all matches are confirmed, recalculate payroll
                    if (uiState.value.uncertainMatches.isEmpty()) {
                        emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.allMatchesConfirmed))
                        delay(AppConstants.Timing.AUTO_RECALC_DELAY_MS)
                        handleAction(PayrollAction.CalculatePayroll)
                    }
                }
                is MatchConfirmationUseCase.ConfirmationResult.Error -> {
                    emitSideEffect(PayrollEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    /**
     * Reject an uncertain match - delegates to MatchConfirmationUseCase
     */
    private fun rejectMatch(match: UncertainMatch) {
        val employeeId = uiState.value.selectedEmployee?.id
        if (employeeId == null) {
            emitSideEffect(PayrollEffect.ShowError("No employee selected"))
            return
        }

        viewModelScope.launch {
            when (val result = matchConfirmationUseCase.rejectMatch(match, employeeId)) {
                is MatchConfirmationUseCase.ConfirmationResult.Rejected -> {
                    // Remove from uncertainMatches list
                    updateState { currentState ->
                        currentState.copy(
                            uncertainMatches = currentState.uncertainMatches - match
                        )
                    }

                    emitSideEffect(
                        PayrollEffect.ShowToast(
                            Strings.Payroll.matchRejected.format(result.eventTitle)
                        )
                    )

                    // If all matches are processed, recalculate payroll
                    if (uiState.value.uncertainMatches.isEmpty()) {
                        emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.allMatchesProcessed))
                        delay(AppConstants.Timing.AUTO_RECALC_DELAY_MS)
                        handleAction(PayrollAction.CalculatePayroll)
                    }
                }
                is MatchConfirmationUseCase.ConfirmationResult.Error -> {
                    emitSideEffect(PayrollEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    /**
     * Add unmatched client - delegates to ClientQuickAddUseCase
     */
    private fun addUnmatchedClient(
        name: String,
        price: Double,
        employeePrice: Double,
        companyPrice: Double,
        employeeId: String?
    ) {
        if (employeeId == null) {
            emitSideEffect(PayrollEffect.ClientAddFailed(name, "No employee selected"))
            return
        }

        viewModelScope.launch {
            when (val result = clientQuickAddUseCase(
                name = name,
                price = price,
                employeePrice = employeePrice,
                companyPrice = companyPrice,
                employeeId = employeeId
            )) {
                is ClientQuickAddUseCase.QuickAddResult.Success -> {
                    emitSideEffect(
                        PayrollEffect.ShowToast(
                            Strings.Payroll.clientAddSuccess.format(result.clientName, price, employeePrice, companyPrice)
                        )
                    )
                    emitSideEffect(PayrollEffect.ClientAdded(result.clientName))
                }
                is ClientQuickAddUseCase.QuickAddResult.Error -> {
                    // Remove from addedClients on failure
                    updateState { currentState ->
                        currentState.copy(
                            addedClients = currentState.addedClients - name
                        )
                    }
                    emitSideEffect(PayrollEffect.ShowError("${Strings.Payroll.clientAddFailed}: ${result.message}"))
                    emitSideEffect(PayrollEffect.ClientAddFailed(result.clientName, result.message))
                }
            }
        }
    }

    /**
     * Sync database from Google Sheets
     */
    private fun syncDatabase() {
        viewModelScope.launch {
            try {
                emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.syncingDatabase))

                val result = databaseSyncService.syncFromSheets()

                result.onSuccess { response ->
                    val syncResult = SyncDatabaseResult(
                        success = true,
                        employeesInserted = response.employeesInserted,
                        employeesUpdated = response.employeesUpdated,
                        clientsInserted = response.clientsInserted,
                        clientsUpdated = response.clientsUpdated,
                        durationMs = response.durationMs
                    )

                    updateState { currentState ->
                        currentState.copy(
                            isSyncing = false,
                            syncResult = syncResult
                        )
                    }

                    // Reload employees after sync
                    loadEmployees()

                    emitSideEffect(
                        PayrollEffect.ShowToast(
                            Strings.Payroll.syncCompleteWithStats.format(
                                response.employeesInserted,
                                response.employeesUpdated,
                                response.clientsInserted,
                                response.clientsUpdated
                            )
                        )
                    )
                    emitSideEffect(PayrollEffect.SyncDatabaseComplete(syncResult))
                }.onFailure { error ->
                    val syncResult = SyncDatabaseResult(
                        success = false,
                        errorMessage = error.message
                    )

                    updateState { currentState ->
                        currentState.copy(
                            isSyncing = false,
                            syncResult = syncResult,
                            error = "${Strings.Payroll.syncError}: ${error.message}"
                        )
                    }

                    emitSideEffect(
                        PayrollEffect.ShowError("${Strings.Payroll.syncError}: ${error.message}")
                    )
                }
            } catch (e: Exception) {
                val syncResult = SyncDatabaseResult(
                    success = false,
                    errorMessage = e.message
                )

                updateState { currentState ->
                    currentState.copy(
                        isSyncing = false,
                        syncResult = syncResult,
                        error = e.message ?: Strings.Payroll.syncError
                    )
                }

                emitSideEffect(PayrollEffect.ShowError("${Strings.Payroll.syncError}: ${e.message}"))
            }
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }

                when (val result = payrollRepository.getAllEmployees()) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            currentState.copy(
                                employees = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                        emitSideEffect(PayrollEffect.ShowToast(Strings.Success.employeesLoaded.format(result.data.size)))
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = "${Strings.Errors.loadEmployeesFailed}: ${result.exception.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: Strings.Errors.loadEmployeesFailed
                    )
                }
            }
        }
    }

    private fun validateSelectedEmployee(employee: Employee) {
        viewModelScope.launch {
            try {
                // Check for employee clients
                when (val result = payrollRepository.getEmployeeClients(employee.id)) {
                    is RepositoryResult.Success -> {
                        val clientCount = result.data.size
                        if (clientCount > 0) {
                            emitSideEffect(
                                PayrollEffect.ShowToast(
                                    Strings.Payroll.employeeSelectedWithClients.format(employee.name, clientCount)
                                )
                            )
                        } else {
                            emitSideEffect(
                                PayrollEffect.ShowError(
                                    Strings.Payroll.employeeHasNoClients.format(employee.name)
                                )
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        emitSideEffect(
                            PayrollEffect.ShowError(
                                Strings.Payroll.errorCheckingClients.format(employee.name)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError(Strings.Payroll.errorValidatingEmployee))
            }
        }
    }

    private fun validateDateRange(state: PayrollState): PayrollState {
        val startDate = state.startDate
        val endDate = state.endDate

        return when {
            startDate == null || endDate == null -> state
            startDate >= endDate -> {
                state.copy(error = Strings.Payroll.startDateMustBeBeforeEnd)
            }
            startDate > Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) -> {
                state.copy(error = Strings.Payroll.startDateCannotBeFuture)
            }
            else -> {
                val daysDifference = endDate.date.toEpochDays() - startDate.date.toEpochDays()
                when {
                    daysDifference > AppConstants.DateRange.MAX_DATE_RANGE_DAYS -> {
                        state.copy(error = Strings.Payroll.dateRangeExceedsMax)
                    }
                    daysDifference > AppConstants.DateRange.LARGE_DATE_RANGE_WARNING_DAYS -> {
                        // Show warning but allow calculation
                        emitSideEffect(
                            PayrollEffect.ShowToast(Strings.Payroll.largeDateRangeWarning)
                        )
                        state.copy(error = null)
                    }
                    else -> state.copy(error = null)
                }
            }
        }
    }

    private fun canCalculatePayroll(state: PayrollState): Boolean {
        return state.selectedEmployee != null &&
                state.startDate != null &&
                state.endDate != null &&
                state.error == null &&
                !state.isCalculating
    }

    private fun getValidationErrorMessage(state: PayrollState): String {
        return when {
            state.selectedEmployee == null -> Strings.Payroll.pleaseSelectEmployee
            state.startDate == null -> Strings.Payroll.pleaseSelectStartDate
            state.endDate == null -> Strings.Payroll.pleaseSelectEndDate
            state.error != null -> state.error
            state.isCalculating -> Strings.Payroll.calculationInProgress
            else -> Strings.Payroll.pleaseFillAllFields
        }
    }

    /**
     * Calculate payroll - delegates to PayrollCalculationUseCase
     */
    private fun calculatePayroll(state: PayrollState) {
        val employee = state.selectedEmployee ?: return
        val startDate = state.startDate ?: return
        val endDate = state.endDate ?: return

        viewModelScope.launch {
            emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.startingCalculation))

            when (val result = payrollCalculationUseCase(employee, startDate, endDate)) {
                is PayrollCalculationUseCase.CalculationResult.Success -> {
                    val uncertainMatches = result.uncertainMatches

                    if (uncertainMatches.isNotEmpty()) {
                        // Show confirmation dialog for uncertain matches
                        updateState { currentState ->
                            currentState.copy(
                                uncertainMatches = uncertainMatches,
                                payrollResult = result.payrollResponse,
                                isCalculating = false
                            )
                        }
                        emitSideEffect(
                            PayrollEffect.ShowToast(
                                Strings.Payroll.uncertainMatchesFound.format(uncertainMatches.size)
                            )
                        )
                    } else {
                        updateState { currentState ->
                            currentState.copy(
                                payrollResult = result.payrollResponse,
                                isCalculating = false,
                                uncertainMatches = emptyList()
                            )
                        }

                        val summary = result.payrollResponse.summary
                        emitSideEffect(
                            PayrollEffect.ShowToast(
                                Strings.Payroll.calculationComplete.format(summary.totalSessions, summary.totalRevenue.toString())
                            )
                        )
                    }
                }
                is PayrollCalculationUseCase.CalculationResult.Error -> {
                    updateState { currentState ->
                        currentState.copy(
                            isCalculating = false,
                            error = "${Strings.Payroll.calculationError}: ${result.message}"
                        )
                    }
                    emitSideEffect(PayrollEffect.ShowError("${Strings.Payroll.calculationError}: ${result.message}"))
                }
            }
        }
    }

    /**
     * Export to PDF - delegates to PayrollExportUseCase
     */
    private fun exportToPdf(result: PayrollResponse) {
        viewModelScope.launch {
            when (val exportResult = payrollExportUseCase.exportToPdf(result)) {
                is ExportResult.Success -> {
                    emitSideEffect(
                        PayrollEffect.ShowToast(
                            Strings.Payroll.pdfCreated.format(exportResult.filePath)
                        )
                    )
                }
                is ExportResult.Error -> {
                    emitSideEffect(PayrollEffect.ShowError("${Strings.Payroll.pdfExportError}: ${exportResult.message}"))
                }
            }
        }
    }

    /**
     * Export to Excel - delegates to PayrollExportUseCase
     */
    private fun exportToExcel(result: PayrollResponse) {
        viewModelScope.launch {
            when (val exportResult = payrollExportUseCase.exportToExcel(result)) {
                is ExportResult.Success -> {
                    emitSideEffect(
                        PayrollEffect.ShowToast(
                            Strings.Payroll.excelCreated.format(exportResult.filePath)
                        )
                    )
                }
                is ExportResult.Error -> {
                    emitSideEffect(PayrollEffect.ShowError("${Strings.Payroll.excelExportError}: ${exportResult.message}"))
                }
            }
        }
    }

    fun confirmAndSyncToSheets(payrollId: String) {
        // TODO: Implement Sheets sync for local mode
        emitSideEffect(PayrollEffect.ShowError(Strings.Payroll.sheetsSyncNotImplemented))
    }

    private fun emitSideEffect(effect: PayrollEffect) {
        viewModelScope.launch {
            _sideEffect.emit(effect)

            // Update state for error dialog
            if (effect is PayrollEffect.ShowError) {
                updateState { currentState ->
                    currentState.copy(
                        showErrorDialog = true,
                        errorDialogMessage = effect.error
                    )
                }
            }
        }
    }

    // Enhanced mock data with more realistic client breakdown
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
                ),
                ClientPayrollDetail(
                    clientName = "Μαρία Κουτίβα",
                    pricePerSession = 50.0,
                    employeePricePerSession = 20.0,
                    companyPricePerSession = 30.0,
                    sessions = 6,
                    totalRevenue = 300.0,
                    employeeEarnings = 120.0,
                    companyEarnings = 180.0
                ),
                ClientPayrollDetail(
                    clientName = "Γιώργος Παπαγιαννέρης",
                    pricePerSession = 50.0,
                    employeePricePerSession = 20.0,
                    companyPricePerSession = 30.0,
                    sessions = 3,
                    totalRevenue = 150.0,
                    employeeEarnings = 60.0,
                    companyEarnings = 90.0
                )
            ),
            generatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                .toString()
        )

    /**
     * Refresh data - reload employees and retry failed operations
     */
    private fun refreshData() {
        Logger.debug("PayrollViewModel", "refreshData() called")
        viewModelScope.launch {
            try {
                Logger.debug("PayrollViewModel", "Starting refresh process")
                emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.refreshingData))

                // Reload employees from database
                Logger.debug("PayrollViewModel", "Loading employees...")
                loadEmployees()

                // If there was an error in calculation, retry
                val currentState = uiState.value
                if (currentState.error != null && currentState.selectedEmployee != null) {
                    delay(AppConstants.Timing.AUTO_RECALC_DELAY_MS) // Small delay after loading employees

                    // Retry payroll calculation if it failed
                    if (currentState.startDate != null && currentState.endDate != null) {
                        emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.retryingCalculation))
                        handleAction(PayrollAction.CalculatePayroll)
                    }
                }

                emitSideEffect(PayrollEffect.ShowToast(Strings.Payroll.refreshComplete))
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError("${Strings.Payroll.refreshError}: ${e.message}"))
            }
        }
    }

    override fun onCleared() {
        // Call parent to cancel viewModelScope
        super.onCleared()
    }
}