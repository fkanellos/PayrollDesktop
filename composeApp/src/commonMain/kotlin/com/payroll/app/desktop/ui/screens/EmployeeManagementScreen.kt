package com.payroll.app.desktop.ui.screens

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
 * Parse hex color string to Color
 * Supports formats: #RRGGBB, #AARRGGBB, RRGGBB, AARRGGBB
 */
private fun parseHexColor(hexColor: String): Color {
    return try {
        val cleanHex = hexColor.removePrefix("#")
        when (cleanHex.length) {
            6 -> Color(
                red = cleanHex.substring(0, 2).toInt(16) / 255f,
                green = cleanHex.substring(2, 4).toInt(16) / 255f,
                blue = cleanHex.substring(4, 6).toInt(16) / 255f
            )
            8 -> Color(
                alpha = cleanHex.substring(0, 2).toInt(16) / 255f,
                red = cleanHex.substring(2, 4).toInt(16) / 255f,
                green = cleanHex.substring(4, 6).toInt(16) / 255f,
                blue = cleanHex.substring(6, 8).toInt(16) / 255f
            )
            else -> PayrollColors.Primary
        }
    } catch (e: Exception) {
        PayrollColors.Primary
    }
}

/**
 * Employee Management Screen
 */
@Composable
fun EmployeeManagementScreen(
    viewModel: EmployeeManagementViewModel = org.koin.compose.koinInject()
) {

    val uiState by viewModel.uiState.collectAsState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is EmployeeManagementEffect.ShowToast -> {
                    println("Toast: ${effect.message}")
                }
                is EmployeeManagementEffect.ShowError -> {
                    println("Error: ${effect.error}")
                }
                else -> { /* Handle other effects */ }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
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
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.handleAction(EmployeeManagementAction.SetSearchQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Αναζήτηση εργαζομένων...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = PayrollColors.TextSecondary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.handleAction(EmployeeManagementAction.SetSearchQuery("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PayrollColors.Primary,
                    unfocusedBorderColor = PayrollColors.DividerColor
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error display
            uiState.error?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = PayrollColors.Error.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = PayrollColors.Error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            color = PayrollColors.Error,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.handleAction(EmployeeManagementAction.ClearError) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PayrollColors.Primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Φόρτωση εργαζομένων...", color = PayrollColors.TextSecondary)
                    }
                }
            } else if (uiState.filteredEmployees.isEmpty()) {
                EmptyEmployeesView(
                    hasSearchQuery = uiState.searchQuery.isNotEmpty(),
                    onAddClick = { viewModel.handleAction(EmployeeManagementAction.ShowAddDialog) }
                )
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
            title = { Text("Διαγραφή Εργαζομένου") },
            text = {
                Text("Είστε σίγουροι ότι θέλετε να διαγράψετε τον εργαζόμενο '${employee.name}';\n\nΑυτή η ενέργεια δεν μπορεί να αναιρεθεί.")
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
                        Text("Διαγραφή")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.handleAction(EmployeeManagementAction.HideDeleteConfirmation) },
                    enabled = !uiState.isSaving
                ) {
                    Text("Ακύρωση")
                }
            }
        )
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
                text = "Διαχείριση Εργαζομένων",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PayrollColors.Primary
            )
            Text(
                text = "$employeeCount εργαζόμενοι στο σύστημα",
                fontSize = 14.sp,
                color = PayrollColors.TextSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onRefreshClick,
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ανανέωση")
            }

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PayrollColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Προσθήκη Εργαζομένου")
            }
        }
    }
}

@Composable
private fun EmptyEmployeesView(
    hasSearchQuery: Boolean,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                if (hasSearchQuery) Icons.Default.SearchOff else Icons.Default.PersonOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = PayrollColors.TextSecondary
            )
            Text(
                text = if (hasSearchQuery) "Δεν βρέθηκαν αποτελέσματα" else "Δεν υπάρχουν εργαζόμενοι",
                fontSize = 18.sp,
                color = PayrollColors.TextSecondary
            )
            Text(
                text = if (hasSearchQuery) "Δοκιμάστε διαφορετικούς όρους αναζήτησης" else "Προσθέστε εργαζόμενους για να ξεκινήσετε",
                fontSize = 14.sp,
                color = PayrollColors.TextSecondary
            )
            if (!hasSearchQuery) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayrollColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Προσθήκη Εργαζομένου")
                }
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
                    modifier = Modifier.weight(2f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PayrollColors.Primary
                )
                Text(
                    "Calendar ID",
                    modifier = Modifier.weight(2f),
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
            modifier = Modifier.weight(2f),
            fontSize = 14.sp,
            color = PayrollColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Calendar ID
        Text(
            employee.calendarId.take(30) + if (employee.calendarId.length > 30) "..." else "",
            modifier = Modifier.weight(2f),
            fontSize = 12.sp,
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

@Composable
private fun EmployeeFormDialog(
    employee: Employee?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var calendarId by remember { mutableStateOf(employee?.calendarId ?: "") }
    var sheetName by remember { mutableStateOf(employee?.sheetName ?: "") }
    var supervisionPrice by remember { mutableStateOf(employee?.supervisionPrice?.toString() ?: "0") }
    var color by remember { mutableStateOf(employee?.color ?: "#2196F3") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var calendarIdError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (employee == null) "Προσθήκη Εργαζομένου" else "Επεξεργασία Εργαζομένου") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.width(500.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Όνομα *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    enabled = !isSaving,
                    supportingText = nameError?.let { { Text(it, color = PayrollColors.Error) } }
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )

                // Calendar ID
                OutlinedTextField(
                    value = calendarId,
                    onValueChange = {
                        calendarId = it
                        calendarIdError = null
                    },
                    label = { Text("Calendar ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = calendarIdError != null,
                    enabled = !isSaving,
                    supportingText = calendarIdError?.let { { Text(it, color = PayrollColors.Error) } }
                )

                // Sheet Name
                OutlinedTextField(
                    value = sheetName,
                    onValueChange = { sheetName = it },
                    label = { Text("Sheet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )

                // Supervision Price
                OutlinedTextField(
                    value = supervisionPrice,
                    onValueChange = { supervisionPrice = it },
                    label = { Text("Τιμή Επίβλεψης (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )

                // Color picker hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Χρώμα (HEX)") },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )

                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = parseHexColor(color),
                        border = BorderStroke(1.dp, PayrollColors.DividerColor)
                    ) {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate
                    var hasError = false

                    if (name.isBlank()) {
                        nameError = "Το όνομα είναι υποχρεωτικό"
                        hasError = true
                    }

                    if (calendarId.isBlank()) {
                        calendarIdError = "Το Calendar ID είναι υποχρεωτικό"
                        hasError = true
                    }

                    if (!hasError) {
                        val newEmployee = Employee(
                            id = employee?.id ?: "",
                            name = name.trim(),
                            email = email.trim(),
                            calendarId = calendarId.trim(),
                            sheetName = sheetName.trim(),
                            supervisionPrice = supervisionPrice.toDoubleOrNull() ?: 0.0,
                            color = color.trim()
                        )
                        onSave(newEmployee)
                    }
                },
                enabled = !isSaving && name.isNotBlank() && calendarId.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "Αποθήκευση..." else "Αποθήκευση")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Ακύρωση")
            }
        }
    )
}

@Preview
@Composable
fun EmployeeManagementScreenPreview() {
    PayrollTheme {
        EmployeeManagementScreen()
    }
}
