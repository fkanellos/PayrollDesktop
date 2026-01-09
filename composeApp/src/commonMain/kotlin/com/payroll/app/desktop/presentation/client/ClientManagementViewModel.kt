package com.payroll.app.desktop.presentation.client

import com.payroll.app.desktop.core.base.BaseViewModel
import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.resources.StringMessage
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.data.repositories.ClientRepository
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.models.Employee
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * ViewModel for Client Management screen
 * Implements MVI pattern with API integration
 */
class ClientManagementViewModel(
    private val payrollRepository: PayrollRepository,
    private val clientRepository: ClientRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) : BaseViewModel<ClientManagementState, ClientManagementAction, ClientManagementEffect>() {

    override val initialState = ClientManagementState()

    private val _sideEffect = MutableSharedFlow<ClientManagementEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        _uiState.value = initialState
        scope.launch {
            delay(AppConstants.Timing.UI_INIT_DELAY_MS)
            handleAction(ClientManagementAction.LoadEmployees)
        }
    }

    override fun reduce(
        currentState: ClientManagementState,
        action: ClientManagementAction
    ): ClientManagementState {
        return when (action) {
            // Loading
            ClientManagementAction.LoadEmployees -> {
                loadEmployees()
                currentState.copy(isLoading = true, error = null)
            }

            ClientManagementAction.RefreshData -> {
                loadEmployees()
                currentState.selectedEmployee?.let { loadClientsForEmployee(it.id) }
                currentState.copy(isLoading = true, error = null)
            }

            // Employee selection
            is ClientManagementAction.SelectEmployee -> {
                loadClientsForEmployee(action.employee.id)
                currentState.copy(
                    selectedEmployee = action.employee,
                    isLoadingClients = true,
                    error = null
                )
            }

            // Search
            is ClientManagementAction.SetSearchQuery -> {
                currentState.copy(searchQuery = action.query)
            }

            // Dialog management
            ClientManagementAction.ShowAddDialog -> {
                currentState.copy(showAddDialog = true)
            }

            ClientManagementAction.HideAddDialog -> {
                currentState.copy(showAddDialog = false)
            }

            is ClientManagementAction.ShowEditDialog -> {
                currentState.copy(editingClient = action.client)
            }

            ClientManagementAction.HideEditDialog -> {
                currentState.copy(editingClient = null)
            }

            is ClientManagementAction.ShowDeleteConfirmation -> {
                currentState.copy(deleteConfirmClient = action.client)
            }

            ClientManagementAction.HideDeleteConfirmation -> {
                currentState.copy(deleteConfirmClient = null)
            }

            // CRUD operations
            is ClientManagementAction.CreateClient -> {
                createClient(action.client)
                currentState.copy(isSaving = true)
            }

            is ClientManagementAction.UpdateClient -> {
                updateClient(action.client)
                currentState.copy(isSaving = true)
            }

            is ClientManagementAction.DeleteClient -> {
                deleteClient(action.clientId)
                currentState.copy(isSaving = true)
            }

            // Sync from Google Sheets
            ClientManagementAction.SyncClientsFromSheets -> {
                syncClientsFromSheets()
                currentState.copy(isSyncing = true, error = null)
            }

            // Error handling
            ClientManagementAction.ClearError -> {
                currentState.copy(error = null)
            }
        }
    }

    private fun loadEmployees() {
        scope.launch {
            try {
                when (val result = payrollRepository.getAllEmployees()) {
                    is RepositoryResult.Success -> {
                        val employees = result.data
                        updateState { currentState ->
                            val selectedEmployee = currentState.selectedEmployee
                                ?: employees.firstOrNull()

                            // If we have a selected employee, load their clients
                            selectedEmployee?.let { loadClientsForEmployee(it.id) }

                            currentState.copy(
                                employees = employees,
                                selectedEmployee = selectedEmployee,
                                isLoading = false,
                                error = null
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowToast(StringMessage.EmployeesLoaded(result.data.size))
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = "${Strings.Errors.loadEmployeesFailed}: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError(StringMessage.LoadEmployeesFailed(result.exception.message ?: "Unknown error"))
                        )
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

    private fun loadClientsForEmployee(employeeId: String) {
        scope.launch {
            try {
                when (val result = clientRepository.getByEmployeeId(employeeId)) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            currentState.copy(
                                clients = result.data,
                                isLoadingClients = false,
                                error = null
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isLoadingClients = false,
                                clients = emptyList(),
                                error = "${Strings.Errors.loadClientsFailed}: ${result.exception.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoadingClients = false,
                        error = e.message ?: Strings.Errors.loadClientsFailed
                    )
                }
            }
        }
    }

    private fun createClient(client: Client) {
        scope.launch {
            try {
                when (val result = clientRepository.createClient(client)) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            currentState.copy(
                                clients = currentState.clients + result.data,
                                isSaving = false,
                                showAddDialog = false
                            )
                        }
                        emitSideEffect(ClientManagementEffect.ClientCreated)
                        emitSideEffect(
                            ClientManagementEffect.ShowToast(StringMessage.ClientAdded)
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "${Strings.Errors.saveFailed}: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError(StringMessage.SaveClientFailed(result.exception.message ?: "Unknown error"))
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(isSaving = false, error = e.message ?: Strings.Errors.saveFailed)
                }
            }
        }
    }

    private fun updateClient(client: Client) {
        scope.launch {
            try {
                when (val result = clientRepository.updateClient(client)) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            currentState.copy(
                                clients = currentState.clients.map {
                                    if (it.id == client.id) result.data else it
                                },
                                isSaving = false,
                                editingClient = null
                            )
                        }
                        emitSideEffect(ClientManagementEffect.ClientUpdated)
                        emitSideEffect(
                            ClientManagementEffect.ShowToast(StringMessage.ClientUpdated)
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "${Strings.Errors.saveFailed}: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError(StringMessage.SaveClientFailed(result.exception.message ?: "Unknown error"))
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(isSaving = false, error = e.message ?: Strings.Errors.saveFailed)
                }
            }
        }
    }

    private fun deleteClient(clientId: Long) {
        scope.launch {
            try {
                when (val result = clientRepository.deleteClient(clientId)) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            currentState.copy(
                                clients = currentState.clients.filter { it.id != clientId },
                                isSaving = false,
                                deleteConfirmClient = null
                            )
                        }
                        emitSideEffect(ClientManagementEffect.ClientDeleted)
                        emitSideEffect(
                            ClientManagementEffect.ShowToast(StringMessage.ClientDeleted)
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "${Strings.Errors.deleteFailed}: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError(StringMessage.DeleteClientFailed(result.exception.message ?: "Unknown error"))
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(isSaving = false, error = e.message ?: Strings.Errors.deleteFailed)
                }
            }
        }
    }

    private fun syncClientsFromSheets() {
        scope.launch {
            val currentState = _uiState.value
            val employee = currentState.selectedEmployee

            if (employee == null) {
                updateState { it.copy(isSyncing = false, error = "No employee selected") }
                return@launch
            }

            if (employee.sheetName.isBlank()) {
                updateState { it.copy(isSyncing = false, error = "Employee has no configured sheet name") }
                return@launch
            }

            try {
                when (val result = clientRepository.syncFromSheets(employee.id, employee.sheetName)) {
                    is RepositoryResult.Success -> {
                        val syncResult = result.data

                        // Reload clients to show updated data
                        loadClientsForEmployee(employee.id)

                        updateState { currentState ->
                            currentState.copy(isSyncing = false)
                        }

                        emitSideEffect(ClientManagementEffect.SyncComplete(syncResult.created, syncResult.updated, syncResult.unchanged))

                        // Show sync complete toast
                        emitSideEffect(ClientManagementEffect.ShowToast(
                            StringMessage.SyncComplete(
                                employeesInserted = 0,
                                employeesUpdated = 0,
                                clientsInserted = syncResult.created,
                                clientsUpdated = syncResult.updated
                            )
                        ))
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSyncing = false,
                                error = Strings.Errors.syncFailed.format(result.exception.message)
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError(StringMessage.SyncFailed(result.exception.message ?: "Unknown error"))
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isSyncing = false,
                        error = Strings.Errors.syncFailed.format(e.message ?: "Unknown error")
                    )
                }
                emitSideEffect(
                    ClientManagementEffect.ShowError(StringMessage.SyncFailed(e.message ?: "Unknown error"))
                )
            }
        }
    }

    private fun emitSideEffect(effect: ClientManagementEffect) {
        scope.launch {
            _sideEffect.emit(effect)
        }
    }

    override fun onCleared() {
        scope.cancel()
    }
}
