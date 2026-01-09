package com.payroll.app.desktop.ui.screens

import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.core.resources.toDisplayString
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.ui.components.shared.errors.ErrorBanner
import com.payroll.app.desktop.ui.components.shared.loading.LoadingIndicator
import com.payroll.app.desktop.ui.components.shared.empty.EmptyStateView
import com.payroll.app.desktop.ui.components.management.clients.ClientFormDialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.domain.usecases.ClientUseCases
import com.payroll.app.desktop.domain.validation.ClientValidator
import com.payroll.app.desktop.domain.validation.ValidationError
import com.payroll.app.desktop.domain.validation.ValidationResult
import com.payroll.app.desktop.presentation.client.ClientManagementAction
import com.payroll.app.desktop.presentation.client.ClientManagementEffect
import com.payroll.app.desktop.presentation.client.ClientManagementViewModel
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import com.payroll.app.desktop.utils.toEuroString
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Client Management Screen
 */
@Composable
fun ClientManagementScreen(
    viewModel: ClientManagementViewModel = org.koin.compose.koinInject()
) {

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ClientManagementEffect.ShowToast -> {
                    val message = effect.message.toDisplayString()
                    Logger.debug("UI", "Toast: $message")
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
                is ClientManagementEffect.ShowError -> {
                    val message = effect.message.toDisplayString()
                    Logger.debug("UI", "Error: $message")
                }
                else -> { /* Handle other effects */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            color = PayrollColors.Background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
            // LEFT PANEL - Employees
            EmployeeListPanel(
                employees = uiState.filteredEmployees,
                selectedEmployeeId = uiState.selectedEmployee?.id,
                searchQuery = uiState.searchQuery,
                isLoading = uiState.isLoading,
                onSearchQueryChange = { viewModel.handleAction(ClientManagementAction.SetSearchQuery(it)) },
                onEmployeeSelected = { employee ->
                    viewModel.handleAction(ClientManagementAction.SelectEmployee(employee))
                }
            )

            // DIVIDER
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = PayrollColors.DividerColor
            )

            // RIGHT PANEL - Clients
            ClientTablePanel(
                employee = uiState.selectedEmployee,
                clients = uiState.clients,
                isLoading = uiState.isLoadingClients,
                isSaving = uiState.isSaving,
                isSyncing = uiState.isSyncing,
                error = uiState.error,
                showAddDialog = uiState.showAddDialog,
                editingClient = uiState.editingClient,
                deleteConfirmClient = uiState.deleteConfirmClient,
                onSyncClick = { viewModel.handleAction(ClientManagementAction.SyncClientsFromSheets) },
                onShowAddDialog = { viewModel.handleAction(ClientManagementAction.ShowAddDialog) },
                onHideAddDialog = { viewModel.handleAction(ClientManagementAction.HideAddDialog) },
                onShowEditDialog = { viewModel.handleAction(ClientManagementAction.ShowEditDialog(it)) },
                onHideEditDialog = { viewModel.handleAction(ClientManagementAction.HideEditDialog) },
                onShowDeleteConfirmation = { viewModel.handleAction(ClientManagementAction.ShowDeleteConfirmation(it)) },
                onHideDeleteConfirmation = { viewModel.handleAction(ClientManagementAction.HideDeleteConfirmation) },
                onCreateClient = { viewModel.handleAction(ClientManagementAction.CreateClient(it)) },
                onUpdateClient = { viewModel.handleAction(ClientManagementAction.UpdateClient(it)) },
                onDeleteClient = { viewModel.handleAction(ClientManagementAction.DeleteClient(it)) },
                onClearError = { viewModel.handleAction(ClientManagementAction.ClearError) }
            )
            }
        }
    }
}

@Composable
fun EmployeeListPanel(
    employees: List<Employee>,
    selectedEmployeeId: String?,
    searchQuery: String,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEmployeeSelected: (Employee) -> Unit
) {
    Column(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(PayrollColors.Surface)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = Strings.EmployeeManagement.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PayrollColors.Primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text(Strings.Common.search) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = PayrollColors.TextSecondary
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PayrollColors.Primary,
                unfocusedBorderColor = PayrollColors.DividerColor
            )
        )

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = PayrollColors.Primary
                )
            }
        }

        // Employee List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(employees) { employee ->
                EmployeeCard(
                    employee = employee,
                    isSelected = employee.id == selectedEmployeeId,
                    onClick = { onEmployeeSelected(employee) }
                )
            }
        }

        // Empty state
        if (!isLoading && employees.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.EmployeeManagement.noEmployees,
                    color = PayrollColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                PayrollColors.Primary.copy(alpha = 0.1f)
            else
                PayrollColors.CardBackground
        ),
        border = if (isSelected) BorderStroke(2.dp, PayrollColors.Primary) else null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isSelected) PayrollColors.Primary else PayrollColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (employee.email.isNotBlank()) {
                    Text(
                        text = employee.email,
                        fontSize = 12.sp,
                        color = PayrollColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = PayrollColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ClientTablePanel(
    employee: Employee?,
    clients: List<Client>,
    isLoading: Boolean,
    isSaving: Boolean,
    isSyncing: Boolean,
    error: String?,
    showAddDialog: Boolean,
    editingClient: Client?,
    deleteConfirmClient: Client?,
    onSyncClick: () -> Unit,
    onShowAddDialog: () -> Unit,
    onHideAddDialog: () -> Unit,
    onShowEditDialog: (Client) -> Unit,
    onHideEditDialog: () -> Unit,
    onShowDeleteConfirmation: (Client) -> Unit,
    onHideDeleteConfirmation: () -> Unit,
    onCreateClient: (Client) -> Unit,
    onUpdateClient: (Client) -> Unit,
    onDeleteClient: (Long) -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PayrollColors.Background)
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = Strings.ClientManagement.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PayrollColors.Primary
                    )
                    if (employee != null) {
                        Surface(
                            color = PayrollColors.Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${clients.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PayrollColors.Primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                employee?.let {
                    Text(
                        text = "Εμφάνιση πελατών για: ${it.name}",
                        fontSize = 14.sp,
                        color = PayrollColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSyncClick,
                    enabled = employee != null && !isSaving && !isSyncing,
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PayrollColors.Primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSyncing) Strings.ClientManagement.syncing else Strings.ClientManagement.syncClients)
                }

                Button(
                    onClick = onShowAddDialog,
                    enabled = employee != null && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayrollColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = Strings.ClientManagement.addClient)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.ClientManagement.addClient)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error display
        error?.let { errorMsg ->
            ErrorBanner(
                message = errorMsg,
                onDismiss = onClearError
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Loading indicator
        if (isLoading) {
            LoadingIndicator(message = Strings.Info.loadingClients)
        } else if (employee == null) {
            // No employee selected
            EmptyStateView(
                icon = Icons.Default.Person,
                title = Strings.ClientManagement.selectEmployee,
                subtitle = Strings.ClientManagement.subtitle
            )
        } else if (clients.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PersonOff,
                title = Strings.ClientManagement.noClients,
                subtitle = Strings.ClientManagement.noClientsForEmployee,
                actionLabel = Strings.ClientManagement.addClient,
                onActionClick = onShowAddDialog
            )
        } else {
            ClientsTable(
                clients = clients,
                onEditClient = onShowEditDialog,
                onDeleteClient = onShowDeleteConfirmation
            )
        }
    }

    // Add Dialog
    if (showAddDialog && employee != null) {
        ClientFormDialog(
            client = null,
            employeeId = employee.id,
            existingClients = clients,
            isSaving = isSaving,
            onDismiss = onHideAddDialog,
            onSave = onCreateClient
        )
    }

    // Edit Dialog
    editingClient?.let { client ->
        ClientFormDialog(
            client = client,
            employeeId = employee?.id ?: "",
            existingClients = clients,
            isSaving = isSaving,
            onDismiss = onHideEditDialog,
            onSave = onUpdateClient
        )
    }

    // Delete Confirmation
    deleteConfirmClient?.let { client ->
        AlertDialog(
            onDismissRequest = onHideDeleteConfirmation,
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = PayrollColors.Error
                )
            },
            title = { Text(Strings.ClientManagement.deleteClient) },
            text = {
                Text(Strings.ClientManagement.confirmDeleteMessage(client.name))
            },
            confirmButton = {
                Button(
                    onClick = { onDeleteClient(client.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayrollColors.Error
                    ),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text(Strings.Common.delete)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onHideDeleteConfirmation,
                    enabled = !isSaving
                ) {
                    Text(Strings.Common.cancel)
                }
            }
        )
    }
}

@Composable
fun EmptyClientsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.PersonOff,
                contentDescription = "No clients",
                modifier = Modifier.size(64.dp),
                tint = PayrollColors.TextSecondary
            )
            Text(
                text = Strings.ClientManagement.noClients,
                fontSize = 18.sp,
                color = PayrollColors.TextSecondary
            )
            Text(
                text = Strings.ClientManagement.noClientsForEmployee,
                fontSize = 14.sp,
                color = PayrollColors.TextSecondary
            )
        }
    }
}

@Composable
fun ClientsTable(
    clients: List<Client>,
    onEditClient: (Client) -> Unit,
    onDeleteClient: (Client) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PayrollColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PayrollColors.Primary.copy(alpha = 0.1f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Όνομα Πελάτη",
                    modifier = Modifier.weight(3f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PayrollColors.Primary
                )
                Text(
                    "Τιμή",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PayrollColors.Primary
                )
                Text(
                    "Εργαζόμενος",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PayrollColors.Primary
                )
                Text(
                    "Εταιρία",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PayrollColors.Primary
                )
                Box(modifier = Modifier.width(100.dp)) {
                    Text(
                        "Ενέργειες",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PayrollColors.Primary
                    )
                }
            }

            HorizontalDivider(color = PayrollColors.DividerColor)

            // Table Rows
            LazyColumn {
                items(clients) { client ->
                    ClientRow(
                        client = client,
                        onEdit = { onEditClient(client) },
                        onDelete = { onDeleteClient(client) }
                    )
                    HorizontalDivider(color = PayrollColors.DividerColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun ClientRow(
    client: Client,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            client.name,
            modifier = Modifier.weight(3f),
            fontSize = 14.sp,
            color = PayrollColors.OnSurface
        )
        Text(
            client.price.toEuroString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PayrollColors.Success
        )
        Text(
            client.employeePrice.toEuroString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = PayrollColors.Info
        )
        Text(
            client.companyPrice.toEuroString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = PayrollColors.Primary
        )

        // Actions
        Row(
            modifier = Modifier.width(100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = PayrollColors.Info
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = PayrollColors.Error
                )
            }
        }
    }
}

// ClientFormDialog and ClientFormFields extracted to ClientFormDialog.kt

@Preview
@Composable
fun ClientManagementScreenPreview() {
    PayrollTheme {
        ClientManagementScreen()
    }
}
