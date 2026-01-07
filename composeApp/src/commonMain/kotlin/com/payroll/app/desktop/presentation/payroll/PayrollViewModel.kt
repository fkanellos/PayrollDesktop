package com.payroll.app.desktop.presentation.payroll

import com.payroll.app.desktop.core.base.BaseViewModel
import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.export.ExportService
import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.models.ClientPayrollDetail
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.domain.models.EmployeeInfo
import com.payroll.app.desktop.domain.models.PayrollRequest
import com.payroll.app.desktop.domain.models.PayrollResponse
import com.payroll.app.desktop.domain.models.PayrollSummary
import com.payroll.app.desktop.domain.models.UncertainMatch
import com.payroll.app.desktop.domain.service.DatabaseSyncService
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
 * Handles all payroll calculation logic and state management
 * Enhanced με calendar support και improved UX
 */
// Interface for MatchConfirmationRepository (to avoid platform-specific dependencies)
interface IMatchConfirmationRepository {
    suspend fun saveConfirmation(eventTitle: String, matchedClientName: String, employeeId: String)
    suspend fun getConfirmedMatch(eventTitle: String, employeeId: String): String?
}

class PayrollViewModel(
    private val payrollRepository: PayrollRepository,
    private val databaseSyncService: DatabaseSyncService,
    private val matchConfirmationRepository: IMatchConfirmationRepository
) : BaseViewModel<PayrollState, PayrollAction, PayrollEffect>() {

    override val initialState = PayrollState()

    private val _sideEffect = MutableSharedFlow<PayrollEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        // Initialize state first
        _uiState.value = initialState

        // Then load data
        viewModelScope.launch {
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
                    emitSideEffect(PayrollEffect.ShowToast("Δημιουργία PDF..."))
                } ?: run {
                    emitSideEffect(PayrollEffect.ShowError("Δεν υπάρχουν αποτελέσματα για εξαγωγή"))
                }
                currentState
            }

            PayrollAction.ExportToExcel -> {
                currentState.payrollResult?.let { result ->
                    exportToExcel(result)
                    emitSideEffect(PayrollEffect.ShowToast("Δημιουργία Excel..."))
                } ?: run {
                    emitSideEffect(PayrollEffect.ShowError("Δεν υπάρχουν αποτελέσματα για εξαγωγή"))
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
        }
    }

    /**
     * Confirm an uncertain match - save to database
     */
    private fun confirmMatch(match: UncertainMatch, employeeId: String?) {
        if (employeeId == null) {
            emitSideEffect(PayrollEffect.ShowError("No employee selected"))
            return
        }

        val suggestedMatch = match.suggestedMatch ?: return

        viewModelScope.launch {
            try {
                // Save confirmation to database
                matchConfirmationRepository.saveConfirmation(
                    eventTitle = match.eventTitle,
                    matchedClientName = suggestedMatch.clientName,
                    employeeId = employeeId
                )

                // Remove from uncertainMatches list
                updateState { currentState ->
                    currentState.copy(
                        uncertainMatches = currentState.uncertainMatches - match
                    )
                }

                emitSideEffect(
                    PayrollEffect.ShowToast(
                        "✅ Αποθηκεύτηκε: '${match.eventTitle}' → '${suggestedMatch.clientName}'"
                    )
                )

                // If all matches are confirmed, recalculate payroll
                if (uiState.value.uncertainMatches.isEmpty()) {
                    emitSideEffect(PayrollEffect.ShowToast("Όλες οι αντιστοιχίες επιβεβαιώθηκαν! Επαναυπολογισμός..."))
                    delay(500)
                    handleAction(PayrollAction.CalculatePayroll)
                }
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError("Σφάλμα αποθήκευσης: ${e.message}"))
            }
        }
    }

    /**
     * Reject an uncertain match - keep event as unmatched
     * Save rejection to database so we don't ask again
     */
    private fun rejectMatch(match: UncertainMatch) {
        val employeeId = uiState.value.selectedEmployee?.id
        if (employeeId == null) {
            emitSideEffect(PayrollEffect.ShowError("No employee selected"))
            return
        }

        viewModelScope.launch {
            try {
                // Save rejection to database (with empty client name to indicate rejection)
                matchConfirmationRepository.saveConfirmation(
                    eventTitle = match.eventTitle,
                    matchedClientName = "__REJECTED__",  // Special marker for rejections
                    employeeId = employeeId
                )

                // Remove from uncertainMatches list
                updateState { currentState ->
                    currentState.copy(
                        uncertainMatches = currentState.uncertainMatches - match
                    )
                }

                emitSideEffect(
                    PayrollEffect.ShowToast(
                        "Απορρίφθηκε: '${match.eventTitle}'"
                    )
                )

                // If all matches are processed, recalculate payroll
                if (uiState.value.uncertainMatches.isEmpty()) {
                    emitSideEffect(PayrollEffect.ShowToast("Όλες οι αντιστοιχίες επεξεργάστηκαν! Επαναυπολογισμός..."))
                    delay(500)
                    handleAction(PayrollAction.CalculatePayroll)
                }
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError("Σφάλμα αποθήκευσης rejection: ${e.message}"))
            }
        }
    }

    /**
     * Add unmatched client to local database
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
            try {
                // Create client via API (will be saved to backend and local DB)
                val newClient = Client(
                    id = 0,
                    name = name,
                    price = price,
                    employeePrice = employeePrice,
                    companyPrice = companyPrice,
                    employeeId = employeeId,
                    pendingPayment = false
                )

                when (val result = payrollRepository.createClient(newClient)) {
                    is RepositoryResult.Success -> {
                        emitSideEffect(
                            PayrollEffect.ShowToast(
                                "✅ Client '$name' added! (€$price: Employee €$employeePrice / Company €$companyPrice)"
                            )
                        )
                        emitSideEffect(PayrollEffect.ClientAdded(name))
                    }
                    is RepositoryResult.Error -> {
                        // Remove from addedClients on failure
                        updateState { currentState ->
                            currentState.copy(
                                addedClients = currentState.addedClients - name
                            )
                        }
                        emitSideEffect(
                            PayrollEffect.ShowError("Failed to add client: ${result.exception.message}")
                        )
                        emitSideEffect(PayrollEffect.ClientAddFailed(name, result.exception.message ?: "Unknown error"))
                    }
                }
            } catch (e: Exception) {
                // Remove from addedClients on failure
                updateState { currentState ->
                    currentState.copy(
                        addedClients = currentState.addedClients - name
                    )
                }
                emitSideEffect(PayrollEffect.ShowError("Error adding client: ${e.message}"))
                emitSideEffect(PayrollEffect.ClientAddFailed(name, e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Sync database from Google Sheets
     */
    private fun syncDatabase() {
        viewModelScope.launch {
            try {
                emitSideEffect(PayrollEffect.ShowToast("🔄 Συγχρονισμός βάσης δεδομένων από Google Sheets..."))

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
                            "✅ Συγχρονισμός ολοκληρώθηκε!\n" +
                                    "Εργαζόμενοι: +${response.employeesInserted} / ↻${response.employeesUpdated}\n" +
                                    "Πελάτες: +${response.clientsInserted} / ↻${response.clientsUpdated}"
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
                            error = "Σφάλμα συγχρονισμού: ${error.message}"
                        )
                    }

                    emitSideEffect(
                        PayrollEffect.ShowError("Σφάλμα συγχρονισμού: ${error.message}")
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
                        error = e.message ?: "Σφάλμα συγχρονισμού"
                    )
                }

                emitSideEffect(PayrollEffect.ShowError("Σφάλμα συγχρονισμού: ${e.message}"))
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
                        emitSideEffect(PayrollEffect.ShowToast("Φορτώθηκαν ${result.data.size} εργαζόμενοι"))
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
                                    "Επιλέχθηκε ${employee.name} - ${clientCount} πελάτες"
                                )
                            )
                        } else {
                            emitSideEffect(
                                PayrollEffect.ShowError(
                                    "Ο εργαζόμενος ${employee.name} δεν έχει καταχωρημένους πελάτες"
                                )
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        emitSideEffect(
                            PayrollEffect.ShowError(
                                "Σφάλμα ελέγχου πελατών για ${employee.name}"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError("Σφάλμα επικύρωσης εργαζομένου"))
            }
        }
    }

    private fun validateDateRange(state: PayrollState): PayrollState {
        val startDate = state.startDate
        val endDate = state.endDate

        return when {
            startDate == null || endDate == null -> state
            startDate >= endDate -> {
                state.copy(error = "Η ημερομηνία έναρξης πρέπει να είναι πριν τη λήξη")
            }
            startDate > Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) -> {
                state.copy(error = "Η ημερομηνία έναρξης δεν μπορεί να είναι στο μέλλον")
            }
            else -> {
                val daysDifference = endDate.date.toEpochDays() - startDate.date.toEpochDays()
                when {
                    daysDifference > 365 -> {
                        state.copy(error = "Το διάστημα δεν μπορεί να υπερβαίνει το 1 έτος")
                    }
                    daysDifference > 90 -> {
                        // Show warning but allow calculation
                        emitSideEffect(
                            PayrollEffect.ShowToast(
                                "⚠️ Μεγάλο χρονικό διάστημα - ο υπολογισμός ίσως αργήσει"
                            )
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
            state.selectedEmployee == null -> "Παρακαλώ επιλέξτε εργαζόμενο"
            state.startDate == null -> "Παρακαλώ επιλέξτε ημερομηνία έναρξης"
            state.endDate == null -> "Παρακαλώ επιλέξτε ημερομηνία λήξης"
            state.error != null -> state.error
            state.isCalculating -> "Υπολογισμός ήδη σε εξέλιξη"
            else -> "Παρακαλώ συμπληρώστε όλα τα απαραίτητα πεδία"
        }
    }

    private fun calculatePayroll(state: PayrollState) {
        val employee = state.selectedEmployee ?: return
        val startDate = state.startDate ?: return
        val endDate = state.endDate ?: return

        viewModelScope.launch {
            try {
                emitSideEffect(PayrollEffect.ShowToast("Έναρξη υπολογισμού μισθοδοσίας..."))

                val request = PayrollRequest(
                    employeeId = employee.id,
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )

                when (val result = payrollRepository.calculatePayroll(request)) {
                    is RepositoryResult.Success -> {
                        // Check if there are uncertain matches
                        val allUncertainMatches = result.data.eventTracking?.uncertainMatches ?: emptyList()

                        Logger.debug("PayrollViewModel", "Found ${allUncertainMatches.size} uncertain matches")
                        allUncertainMatches.forEach { match ->
                            Logger.debug("PayrollViewModel", "  - ${match.eventTitle} → ${match.suggestedMatch?.clientName} (${match.suggestedMatch?.confidence})")
                        }

                        // Filter out matches that have already been confirmed OR rejected
                        val uncertainMatches = allUncertainMatches.filter { match ->
                            val confirmed = matchConfirmationRepository.getConfirmedMatch(
                                eventTitle = match.eventTitle,
                                employeeId = employee.id
                            )
                            val isFiltered = confirmed != null
                            if (isFiltered) {
                                val action = if (confirmed == "__REJECTED__") "rejected" else "confirmed as '$confirmed'"
                                Logger.debug("PayrollViewModel", "Filtered out '${match.eventTitle}' (already $action)")
                            }
                            confirmed == null  // Only keep if NOT confirmed/rejected yet
                        }

                        Logger.debug("PayrollViewModel", "After filtering: ${uncertainMatches.size} uncertain matches remaining")

                        if (uncertainMatches.isNotEmpty()) {
                            // Show confirmation dialog for uncertain matches
                            updateState { currentState ->
                                currentState.copy(
                                    uncertainMatches = uncertainMatches,
                                    payrollResult = result.data,
                                    isCalculating = false
                                )
                            }
                            emitSideEffect(
                                PayrollEffect.ShowToast(
                                    "⚠️ Βρέθηκαν ${uncertainMatches.size} αβέβαιες αντιστοιχίες που χρειάζονται επιβεβαίωση"
                                )
                            )
                        } else {
                            updateState { currentState ->
                                currentState.copy(
                                    payrollResult = result.data,
                                    isCalculating = false,
                                    uncertainMatches = emptyList()
                                )
                            }

                            val summary = result.data.summary
                            emitSideEffect(
                                PayrollEffect.ShowToast(
                                    "✅ Υπολογισμός ολοκληρώθηκε! ${summary.totalSessions} συνεδρίες, ${summary.totalRevenue.toString()}€"
                                )
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isCalculating = false,
                                error = "Σφάλμα υπολογισμού: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(PayrollEffect.ShowError("Σφάλμα υπολογισμού: ${result.exception.message}"))
                    }
                }

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
        // TODO: Implement local PDF export
        emitSideEffect(PayrollEffect.ShowError("PDF export not yet implemented for local mode"))
    }

    private fun exportToExcel(result: PayrollResponse) {
        // TODO: Implement local Excel export
        emitSideEffect(PayrollEffect.ShowError("Excel export not yet implemented for local mode"))
    }

    fun confirmAndSyncToSheets(payrollId: String) {
        // TODO: Implement Sheets sync for local mode
        emitSideEffect(PayrollEffect.ShowError("Sheets sync not yet implemented for local mode"))
    }

    private fun emitSideEffect(effect: PayrollEffect) {
        viewModelScope.launch {
            _sideEffect.emit(effect)
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
                emitSideEffect(PayrollEffect.ShowToast("🔄 Ανανέωση δεδομένων..."))

                // Reload employees from database
                Logger.debug("PayrollViewModel", "Loading employees...")
                loadEmployees()

                // If there was an error in calculation, retry
                val currentState = uiState.value
                if (currentState.error != null && currentState.selectedEmployee != null) {
                    delay(500) // Small delay after loading employees

                    // Retry payroll calculation if it failed
                    if (currentState.startDate != null && currentState.endDate != null) {
                        emitSideEffect(PayrollEffect.ShowToast("🔄 Επανάληψη υπολογισμού..."))
                        handleAction(PayrollAction.CalculatePayroll)
                    }
                }

                emitSideEffect(PayrollEffect.ShowToast("✅ Ανανέωση ολοκληρώθηκε"))
            } catch (e: Exception) {
                emitSideEffect(PayrollEffect.ShowError("Σφάλμα ανανέωσης: ${e.message}"))
            }
        }
    }

    override fun onCleared() {
        // Call parent to cancel viewModelScope
        super.onCleared()
    }
}