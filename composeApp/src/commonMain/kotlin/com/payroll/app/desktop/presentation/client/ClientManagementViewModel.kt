package com.payroll.app.desktop.presentation.client

import com.payroll.app.desktop.core.base.BaseViewModel
import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.core.base.RepositoryResult
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
                            ClientManagementEffect.ShowToast("Φορτώθηκαν ${result.data.size} εργαζόμενοι")
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = "Σφάλμα φόρτωσης εργαζομένων: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError("Σφάλμα φόρτωσης εργαζομένων")
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Σφάλμα φόρτωσης"
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
                                error = "Σφάλμα φόρτωσης πελατών: ${result.exception.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoadingClients = false,
                        error = e.message
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
                            ClientManagementEffect.ShowToast("Ο πελάτης '${client.name}' δημιουργήθηκε")
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "Σφάλμα δημιουργίας: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError("Σφάλμα δημιουργίας πελάτη")
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(isSaving = false, error = e.message)
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
                            ClientManagementEffect.ShowToast("Ο πελάτης '${client.name}' ενημερώθηκε")
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "Σφάλμα ενημέρωσης: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError("Σφάλμα ενημέρωσης πελάτη")
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(isSaving = false, error = e.message)
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
                            ClientManagementEffect.ShowToast("Ο πελάτης διαγράφηκε")
                        )
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "Σφάλμα διαγραφής: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(
                            ClientManagementEffect.ShowError("Σφάλμα διαγραφής πελάτη")
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(isSaving = false, error = e.message)
                }
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
