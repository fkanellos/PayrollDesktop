package com.payroll.app.desktop.ui.screens

import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.core.resources.toDisplayString
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.parseHexColor
import com.payroll.app.desktop.ui.components.shared.errors.ErrorBanner
import com.payroll.app.desktop.ui.components.shared.loading.LoadingIndicator
import com.payroll.app.desktop.ui.components.shared.empty.EmptyStateView
import com.payroll.app.desktop.ui.components.shared.search.SearchBar
import com.payroll.app.desktop.ui.components.management.employees.EmployeeFormDialog

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
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.presentation.employee.EmployeeManagementAction
import com.payroll.app.desktop.presentation.employee.EmployeeManagementEffect
import com.payroll.app.desktop.presentation.employee.EmployeeManagementViewModel
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Employee Management Screen
 */
@Composable
fun EmployeeManagementScreen(
    viewModel: EmployeeManagementViewModel = org.koin.compose.koinInject()
) {

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is EmployeeManagementEffect.ShowToast -> {
                    val message = effect.message.toDisplayString()
                    Logger.debug("UI", "Toast: $message")
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
                is EmployeeManagementEffect.ShowError -> {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
        ) {
            // Header
            EmployeeManagementHeader(
                employeeCount = uiState.employees.size,
                isLoading = uiState.isLoading,
                onAddClick = { viewModel.handleAction(EmployeeManagementAction.ShowAddDialog) },
                onRefreshClick = { viewModel.handleAction(EmployeeManagementAction.RefreshEmployees) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Box
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.handleAction(EmployeeManagementAction.SetSearchQuery(it)) },
                placeholder = Strings.Common.search
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error display
            uiState.error?.let { errorMsg ->
                ErrorBanner(
                    message = errorMsg,
                    onDismiss = { viewModel.handleAction(EmployeeManagementAction.ClearError) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading indicator
            if (uiState.isLoading) {
                LoadingIndicator(message = Strings.Info.loadingEmployees)
            } else if (uiState.filteredEmployees.isEmpty()) {
                if (uiState.searchQuery.isNotEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.SearchOff,
                        title = "Δεν βρέθηκαν αποτελέσματα",
                        subtitle = "Δοκιμάστε διαφορετικούς όρους αναζήτησης"
                    )
                } else {
                    EmptyStateView(
                        icon = Icons.Default.PersonOff,
                        title = "Δεν υπάρχουν εργαζόμενοι",
                        subtitle = "Προσθέστε εργαζόμενους για να ξεκινήσετε",
                        actionLabel = Strings.EmployeeManagement.addEmployee,
                        onActionClick = { viewModel.handleAction(EmployeeManagementAction.ShowAddDialog) }
                    )
                }
            } else {
                EmployeesTable(
                    employees = uiState.filteredEmployees,
                    selectedEmployeeId = uiState.selectedEmployee?.id,
                    onSelectEmployee = { viewModel.handleAction(EmployeeManagementAction.SelectEmployee(it)) },
                    onEditEmployee = { viewModel.handleAction(EmployeeManagementAction.ShowEditDialog(it)) },
                    onDeleteEmployee = { viewModel.handleAction(EmployeeManagementAction.ShowDeleteConfirmation(it)) }
                )
            }
        }
    }

    // Add Dialog
    if (uiState.showAddDialog) {
        EmployeeFormDialog(
            employee = null,
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.handleAction(EmployeeManagementAction.HideAddDialog) },
            onSave = { viewModel.handleAction(EmployeeManagementAction.CreateEmployee(it)) }
        )
    }

    // Edit Dialog
    uiState.editingEmployee?.let { employee ->
        EmployeeFormDialog(
            employee = employee,
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.handleAction(EmployeeManagementAction.HideEditDialog) },
            onSave = { viewModel.handleAction(EmployeeManagementAction.UpdateEmployee(it)) }
        )
    }

    // Delete Confirmation
    uiState.deleteConfirmEmployee?.let { employee ->
        AlertDialog(
            onDismissRequest = { viewModel.handleAction(EmployeeManagementAction.HideDeleteConfirmation) },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = PayrollColors.Error
                )
            },
            title = { Text(Strings.EmployeeManagement.deleteEmployee) },
            text = {
                Text(Strings.EmployeeManagement.confirmDeleteMessage.format(employee.name))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.handleAction(EmployeeManagementAction.DeleteEmployee(employee.id)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayrollColors.Error
                    ),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
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
                    onClick = { viewModel.handleAction(EmployeeManagementAction.HideDeleteConfirmation) },
                    enabled = !uiState.isSaving
                ) {
                    Text(Strings.Common.cancel)
                }
            }
        )
    }
    }
}

@Composable
private fun EmployeeManagementHeader(
    employeeCount: Int,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = Strings.EmployeeManagement.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PayrollColors.Primary
            )
            Text(
                text = Strings.Success.employeesLoaded.format(employeeCount),
                fontSize = 14.sp,
                color = PayrollColors.TextSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onRefreshClick,
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = Strings.Common.refresh)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Strings.Common.refresh)
            }

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PayrollColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = Strings.EmployeeManagement.addEmployee)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Strings.EmployeeManagement.addEmployee)
            }
        }
    }
}

@Composable
private fun EmployeesTable(
    employees: List<Employee>,
    selectedEmployeeId: String?,
    onSelectEmployee: (Employee) -> Unit,
    onEditEmployee: (Employee) -> Unit,
    onDeleteEmployee: (Employee) -> Unit
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
                    "Όνομα",
                    modifier = Modifier.weight(2f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PayrollColors.Primary
                )
                Text(
                    "Email",
                    modifier = Modifier.weight(3f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PayrollColors.Primary
                )
                Text(
                    "Τιμή Επίβλεψης",
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
                items(employees) { employee ->
                    val isSelected = employee.id == selectedEmployeeId

                    EmployeeRow(
                        employee = employee,
                        isSelected = isSelected,
                        onClick = { onSelectEmployee(employee) },
                        onEdit = { onEditEmployee(employee) },
                        onDelete = { onDeleteEmployee(employee) }
                    )
                    HorizontalDivider(color = PayrollColors.DividerColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun EmployeeRow(
    employee: Employee,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) PayrollColors.Primary.copy(alpha = 0.05f)
                else Color.Transparent
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name
        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Surface(
                modifier = Modifier.size(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = parseHexColor(employee.color)
            ) {}

            Text(
                employee.name,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) PayrollColors.Primary else PayrollColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Email
        Text(
            employee.email.ifBlank { "-" },
            modifier = Modifier.weight(3f),
            fontSize = 14.sp,
            color = PayrollColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Supervision Price
        Text(
            "€${employee.supervisionPrice}",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PayrollColors.Success
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

// EmployeeFormDialog has been extracted to EmployeeFormDialog.kt

@Preview
@Composable
fun EmployeeManagementScreenPreview() {
    PayrollTheme {
        EmployeeManagementScreen()
    }
}
