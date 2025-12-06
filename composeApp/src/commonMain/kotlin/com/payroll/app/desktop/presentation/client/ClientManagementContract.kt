package com.payroll.app.desktop.presentation.client

import com.payroll.app.desktop.core.base.UiAction
import com.payroll.app.desktop.core.base.UiEffect
import com.payroll.app.desktop.core.base.UiState
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.models.Employee

/**
 * ClientManagementState - All possible states for the client management screen
 */
data class ClientManagementState(
    val isLoading: Boolean = false,
    val isLoadingClients: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val employees: List<Employee> = emptyList(),
    val selectedEmployee: Employee? = null,
    val clients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val editingClient: Client? = null,
    val deleteConfirmClient: Client? = null
) : UiState {

    val filteredEmployees: List<Employee>
        get() = if (searchQuery.isBlank()) {
            employees
        } else {
            employees.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
            }
        }
}

/**
 * ClientManagementAction - All user actions
 */
sealed class ClientManagementAction : UiAction {
    // Data loading
    object LoadEmployees : ClientManagementAction()
    object RefreshData : ClientManagementAction()

    // Employee selection
    data class SelectEmployee(val employee: Employee) : ClientManagementAction()

    // Search
    data class SetSearchQuery(val query: String) : ClientManagementAction()

    // Client CRUD
    object ShowAddDialog : ClientManagementAction()
    object HideAddDialog : ClientManagementAction()
    data class ShowEditDialog(val client: Client) : ClientManagementAction()
    object HideEditDialog : ClientManagementAction()
    data class ShowDeleteConfirmation(val client: Client) : ClientManagementAction()
    object HideDeleteConfirmation : ClientManagementAction()

    data class CreateClient(val client: Client) : ClientManagementAction()
    data class UpdateClient(val client: Client) : ClientManagementAction()
    data class DeleteClient(val clientId: Long) : ClientManagementAction()

    // Error handling
    object ClearError : ClientManagementAction()
}

/**
 * ClientManagementEffect - Side effects
 */
sealed class ClientManagementEffect : UiEffect {
    data class ShowToast(val message: String) : ClientManagementEffect()
    data class ShowError(val error: String) : ClientManagementEffect()
    object ClientCreated : ClientManagementEffect()
    object ClientUpdated : ClientManagementEffect()
    object ClientDeleted : ClientManagementEffect()
}
