package com.payroll.app.desktop.presentation.employee

import com.payroll.app.desktop.core.base.BaseViewModel
import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.data.repositories.EmployeeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * ViewModel for Employee Management Screen
 * Handles CRUD operations for employees
 */
class EmployeeManagementViewModel(
    private val employeeRepository: EmployeeRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) : BaseViewModel<EmployeeManagementState, EmployeeManagementAction, EmployeeManagementEffect>() {

    override val initialState = EmployeeManagementState()

    private val _sideEffect = MutableSharedFlow<EmployeeManagementEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        _uiState.value = initialState
        scope.launch {
            delay(100)
            handleAction(EmployeeManagementAction.LoadEmployees)
        }
    }

    override fun reduce(
        currentState: EmployeeManagementState,
        action: EmployeeManagementAction
    ): EmployeeManagementState {
        return when (action) {
            // Loading
            EmployeeManagementAction.LoadEmployees -> {
                loadEmployees()
                currentState.copy(isLoading = true, error = null)
            }

            EmployeeManagementAction.RefreshEmployees -> {
                loadEmployees()
                currentState.copy(isLoading = true, error = null)
            }

            // Search/Filter
            is EmployeeManagementAction.SetSearchQuery -> {
                val filtered = filterEmployees(currentState.employees, action.query)
                currentState.copy(
                    searchQuery = action.query,
                    filteredEmployees = filtered
                )
            }

            // Selection
            is EmployeeManagementAction.SelectEmployee -> {
                currentState.copy(selectedEmployee = action.employee)
            }

            EmployeeManagementAction.ClearSelection -> {
                currentState.copy(selectedEmployee = null)
            }

            // Dialog Actions
            EmployeeManagementAction.ShowAddDialog -> {
                currentState.copy(showAddDialog = true)
            }

            EmployeeManagementAction.HideAddDialog -> {
                currentState.copy(showAddDialog = false)
            }

            is EmployeeManagementAction.ShowEditDialog -> {
                currentState.copy(editingEmployee = action.employee)
            }

            EmployeeManagementAction.HideEditDialog -> {
                currentState.copy(editingEmployee = null)
            }

            is EmployeeManagementAction.ShowDeleteConfirmation -> {
                currentState.copy(deleteConfirmEmployee = action.employee)
            }

            EmployeeManagementAction.HideDeleteConfirmation -> {
                currentState.copy(deleteConfirmEmployee = null)
            }

            // CRUD Actions
            is EmployeeManagementAction.CreateEmployee -> {
                createEmployee(action.employee)
                currentState.copy(isSaving = true, error = null)
            }

            is EmployeeManagementAction.UpdateEmployee -> {
                updateEmployee(action.employee)
                currentState.copy(isSaving = true, error = null)
            }

            is EmployeeManagementAction.DeleteEmployee -> {
                deleteEmployee(action.employeeId)
                currentState.copy(isSaving = true, error = null)
            }

            // Error handling
            EmployeeManagementAction.ClearError -> {
                currentState.copy(error = null)
            }
        }
    }

    private fun filterEmployees(employees: List<com.payroll.app.desktop.domain.models.Employee>, query: String): List<com.payroll.app.desktop.domain.models.Employee> {
        if (query.isBlank()) return employees
        val lowerQuery = query.lowercase()
        return employees.filter { employee ->
            employee.name.lowercase().contains(lowerQuery) ||
                    employee.email.lowercase().contains(lowerQuery)
        }
    }

    private fun loadEmployees() {
        scope.launch {
            try {
                when (val result = employeeRepository.getAll()) {
                    is RepositoryResult.Success -> {
                        val employees = result.data
                        updateState { currentState ->
                            currentState.copy(
                                employees = employees,
                                filteredEmployees = filterEmployees(employees, currentState.searchQuery),
                                isLoading = false,
                                error = null
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowToast("Φορτώθηκαν ${employees.size} εργαζόμενοι"))
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = "Σφάλμα φόρτωσης εργαζομένων: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowError("Σφάλμα φόρτωσης: ${result.exception.message}"))
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

    private fun createEmployee(employee: com.payroll.app.desktop.domain.models.Employee) {
        scope.launch {
            try {
                when (val result = employeeRepository.createEmployee(employee)) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            val updatedList = currentState.employees + result.data
                            currentState.copy(
                                employees = updatedList,
                                filteredEmployees = filterEmployees(updatedList, currentState.searchQuery),
                                showAddDialog = false,
                                isSaving = false
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowToast("Ο εργαζόμενος δημιουργήθηκε επιτυχώς"))
                        emitSideEffect(EmployeeManagementEffect.EmployeeCreated)
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "Σφάλμα δημιουργίας: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowError("Σφάλμα δημιουργίας: ${result.exception.message}"))
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isSaving = false,
                        error = e.message ?: "Σφάλμα δημιουργίας"
                    )
                }
            }
        }
    }

    private fun updateEmployee(employee: com.payroll.app.desktop.domain.models.Employee) {
        scope.launch {
            try {
                when (val result = employeeRepository.updateEmployee(employee)) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            val updatedList = currentState.employees.map {
                                if (it.id == employee.id) result.data else it
                            }
                            currentState.copy(
                                employees = updatedList,
                                filteredEmployees = filterEmployees(updatedList, currentState.searchQuery),
                                editingEmployee = null,
                                isSaving = false
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowToast("Ο εργαζόμενος ενημερώθηκε επιτυχώς"))
                        emitSideEffect(EmployeeManagementEffect.EmployeeUpdated)
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                error = "Σφάλμα ενημέρωσης: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowError("Σφάλμα ενημέρωσης: ${result.exception.message}"))
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isSaving = false,
                        error = e.message ?: "Σφάλμα ενημέρωσης"
                    )
                }
            }
        }
    }

    private fun deleteEmployee(employeeId: String) {
        scope.launch {
            try {
                when (val result = employeeRepository.deleteEmployee(employeeId)) {
                    is RepositoryResult.Success -> {
                        updateState { currentState ->
                            val updatedList = currentState.employees.filter { it.id != employeeId }
                            currentState.copy(
                                employees = updatedList,
                                filteredEmployees = filterEmployees(updatedList, currentState.searchQuery),
                                deleteConfirmEmployee = null,
                                selectedEmployee = if (currentState.selectedEmployee?.id == employeeId) null else currentState.selectedEmployee,
                                isSaving = false
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowToast("Ο εργαζόμενος διαγράφηκε επιτυχώς"))
                        emitSideEffect(EmployeeManagementEffect.EmployeeDeleted)
                    }
                    is RepositoryResult.Error -> {
                        updateState { currentState ->
                            currentState.copy(
                                isSaving = false,
                                deleteConfirmEmployee = null,
                                error = "Σφάλμα διαγραφής: ${result.exception.message}"
                            )
                        }
                        emitSideEffect(EmployeeManagementEffect.ShowError("Σφάλμα διαγραφής: ${result.exception.message}"))
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isSaving = false,
                        deleteConfirmEmployee = null,
                        error = e.message ?: "Σφάλμα διαγραφής"
                    )
                }
            }
        }
    }

    private fun emitSideEffect(effect: EmployeeManagementEffect) {
        scope.launch {
            _sideEffect.emit(effect)
        }
    }

    override fun onCleared() {
        scope.cancel()
    }
}
