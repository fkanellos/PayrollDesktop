package com.payroll.app.desktop.presentation.employee

import com.payroll.app.desktop.core.base.UiAction
import com.payroll.app.desktop.core.base.UiEffect
import com.payroll.app.desktop.core.base.UiState
import com.payroll.app.desktop.domain.models.Employee

/**
 * Employee Management State
 */
data class EmployeeManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val employees: List<Employee> = emptyList(),
    val filteredEmployees: List<Employee> = emptyList(),
    val selectedEmployee: Employee? = null,
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val editingEmployee: Employee? = null,
    val deleteConfirmEmployee: Employee? = null,
    val isSaving: Boolean = false
) : UiState

/**
 * Employee Management Actions
 */
sealed class EmployeeManagementAction : UiAction {
    // Loading
    object LoadEmployees : EmployeeManagementAction()
    object RefreshEmployees : EmployeeManagementAction()

    // Search/Filter
    data class SetSearchQuery(val query: String) : EmployeeManagementAction()

    // Selection
    data class SelectEmployee(val employee: Employee) : EmployeeManagementAction()
    object ClearSelection : EmployeeManagementAction()

    // Dialog Actions
    object ShowAddDialog : EmployeeManagementAction()
    object HideAddDialog : EmployeeManagementAction()
    data class ShowEditDialog(val employee: Employee) : EmployeeManagementAction()
    object HideEditDialog : EmployeeManagementAction()
    data class ShowDeleteConfirmation(val employee: Employee) : EmployeeManagementAction()
    object HideDeleteConfirmation : EmployeeManagementAction()

    // CRUD Actions
    data class CreateEmployee(val employee: Employee) : EmployeeManagementAction()
    data class UpdateEmployee(val employee: Employee) : EmployeeManagementAction()
    data class DeleteEmployee(val employeeId: String) : EmployeeManagementAction()

    // Error handling
    object ClearError : EmployeeManagementAction()
}

/**
 * Employee Management Effects
 */
sealed class EmployeeManagementEffect : UiEffect {
    data class ShowToast(val message: String) : EmployeeManagementEffect()
    data class ShowError(val error: String) : EmployeeManagementEffect()
    object EmployeeCreated : EmployeeManagementEffect()
    object EmployeeUpdated : EmployeeManagementEffect()
    object EmployeeDeleted : EmployeeManagementEffect()
}
