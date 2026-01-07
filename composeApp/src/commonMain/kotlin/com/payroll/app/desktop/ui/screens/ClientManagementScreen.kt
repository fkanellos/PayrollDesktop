package com.payroll.app.desktop.ui.screens

import com.payroll.app.desktop.core.logging.Logger

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

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ClientManagementEffect.ShowToast -> {
                    Logger.debug("UI", "Toast: ${effect.message}")
                }
                is ClientManagementEffect.ShowError -> {
                    Logger.debug("UI", "Error: ${effect.error}")
                }
                else -> { /* Handle other effects */ }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                error = uiState.error,
                showAddDialog = uiState.showAddDialog,
                editingClient = uiState.editingClient,
                deleteConfirmClient = uiState.deleteConfirmClient,
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
            text = "Εργαζόμενοι",
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
            placeholder = { Text("Αναζήτηση...") },
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
                    text = "Δεν βρέθηκαν εργαζόμενοι",
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
    error: String?,
    showAddDialog: Boolean,
    editingClient: Client?,
    deleteConfirmClient: Client?,
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
                Text(
                    text = "Πελάτες",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayrollColors.Primary
                )
                employee?.let {
                    Text(
                        text = "Εμφάνιση πελατών για: ${it.name}",
                        fontSize = 14.sp,
                        color = PayrollColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Button(
                onClick = onShowAddDialog,
                enabled = employee != null && !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PayrollColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Προσθήκη Πελάτη")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error display
        error?.let { errorMsg ->
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
                    IconButton(onClick = onClearError) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PayrollColors.Primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Φόρτωση πελατών...", color = PayrollColors.TextSecondary)
                }
            }
        } else if (employee == null) {
            // No employee selected
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = PayrollColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Επιλέξτε εργαζόμενο",
                        fontSize = 18.sp,
                        color = PayrollColors.TextSecondary
                    )
                }
            }
        } else if (clients.isEmpty()) {
            EmptyClientsView()
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
            title = { Text("Διαγραφή Πελάτη") },
            text = {
                Text("Είστε σίγουροι ότι θέλετε να διαγράψετε τον πελάτη '${client.name}';")
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
                        Text("Διαγραφή")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onHideDeleteConfirmation,
                    enabled = !isSaving
                ) {
                    Text("Ακύρωση")
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
                text = "Δεν υπάρχουν πελάτες",
                fontSize = 18.sp,
                color = PayrollColors.TextSecondary
            )
            Text(
                text = "Προσθέστε πελάτες με το κουμπί παραπάνω",
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

@Composable
fun ClientFormDialog(
    client: Client?,
    employeeId: String,
    existingClients: List<Client>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Client) -> Unit
) {
    var price by remember { mutableStateOf(client?.price?.toString() ?: "") }
    var employeePrice by remember { mutableStateOf(client?.employeePrice?.toString() ?: "") }
    var companyPrice by remember { mutableStateOf(client?.companyPrice?.toString() ?: "") }
    var name by remember { mutableStateOf(client?.name ?: "") }

    var validationErrors by remember { mutableStateOf<List<ValidationError>>(emptyList()) }
    val clientUseCases = remember { ClientUseCases() }

    // Auto-calculate company price
    var autoCalculateCompany by remember { mutableStateOf(client == null) }

    LaunchedEffect(price, employeePrice) {
        if (autoCalculateCompany && price.isNotBlank() && employeePrice.isNotBlank()) {
            val totalPrice = price.toDoubleOrNull() ?: 0.0
            val empPrice = employeePrice.toDoubleOrNull() ?: 0.0
            val calculated = clientUseCases.calculateCompanyPrice(totalPrice, empPrice)
            companyPrice = if (calculated >= 0) calculated.toString() else ""
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (client == null) "Προσθήκη Πελάτη" else "Επεξεργασία Πελάτη") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.width(400.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationErrors = validationErrors.filter { e -> e.field != "name" }
                    },
                    label = { Text("Όνομα Πελάτη *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == "name" },
                    enabled = !isSaving,
                    supportingText = {
                        validationErrors.find { it.field == "name" }?.let {
                            Text(it.message, color = PayrollColors.Error)
                        }
                    }
                )

                // Total Price
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        validationErrors = validationErrors.filter { e -> e.field != "price" }
                    },
                    label = { Text("Τιμή Πελάτη (€) *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == "price" },
                    enabled = !isSaving,
                    supportingText = {
                        validationErrors.find { it.field == "price" }?.let {
                            Text(it.message, color = PayrollColors.Error)
                        }
                    }
                )

                HorizontalDivider(color = PayrollColors.DividerColor)

                // Employee Price
                OutlinedTextField(
                    value = employeePrice,
                    onValueChange = {
                        employeePrice = it
                        validationErrors = validationErrors.filter { e -> e.field != "employeePrice" }
                    },
                    label = { Text("Τιμή Εργαζόμενου (€) *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == "employeePrice" },
                    enabled = !isSaving,
                    supportingText = {
                        validationErrors.find { it.field == "employeePrice" }?.let {
                            Text(it.message, color = PayrollColors.Error)
                        }
                    }
                )

                // Company Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = companyPrice,
                        onValueChange = {
                            companyPrice = it
                            autoCalculateCompany = false
                            validationErrors = validationErrors.filter { e -> e.field != "companyPrice" }
                        },
                        label = { Text("Τιμή Εταιρίας (€) *") },
                        modifier = Modifier.weight(1f),
                        isError = validationErrors.any { it.field == "companyPrice" },
                        enabled = !isSaving,
                        supportingText = {
                            validationErrors.find { it.field == "companyPrice" }?.let {
                                Text(it.message, color = PayrollColors.Error)
                            }
                        }
                    )

                    IconButton(
                        onClick = {
                            autoCalculateCompany = true
                            val totalPrice = price.toDoubleOrNull() ?: 0.0
                            val empPrice = employeePrice.toDoubleOrNull() ?: 0.0
                            companyPrice = clientUseCases.calculateCompanyPrice(totalPrice, empPrice).toString()
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Auto-calculate",
                            tint = if (autoCalculateCompany) PayrollColors.Primary else PayrollColors.TextSecondary
                        )
                    }
                }

                // Validation hint
                if (validationErrors.isEmpty() && price.isNotBlank() && employeePrice.isNotBlank() && companyPrice.isNotBlank()) {
                    val totalPrice = price.toDoubleOrNull() ?: 0.0
                    val empPrice = employeePrice.toDoubleOrNull() ?: 0.0
                    val compPrice = companyPrice.toDoubleOrNull() ?: 0.0
                    val sum = empPrice + compPrice
                    val diff = kotlin.math.abs(totalPrice - sum)

                    Surface(
                        color = if (diff <= 0.01) PayrollColors.Success.copy(alpha = 0.1f)
                        else PayrollColors.Warning.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (diff <= 0.01) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (diff <= 0.01) PayrollColors.Success else PayrollColors.Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Άθροισμα: ${empPrice.toEuroString()} + ${compPrice.toEuroString()} = ${sum.toEuroString()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (diff <= 0.01) PayrollColors.Success else PayrollColors.Warning
                                )
                                if (diff > 0.01) {
                                    Text(
                                        text = "Αναμενόμενο: ${totalPrice.toEuroString()} (διαφορά: ${diff.toEuroString()})",
                                        fontSize = 11.sp,
                                        color = PayrollColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newClient = Client(
                        id = client?.id ?: 0L,
                        name = name.trim(),
                        price = price.toDoubleOrNull() ?: 0.0,
                        employeePrice = employeePrice.toDoubleOrNull() ?: 0.0,
                        companyPrice = companyPrice.toDoubleOrNull() ?: 0.0,
                        employeeId = employeeId,
                        pendingPayment = client?.pendingPayment ?: false
                    )

                    // Validate using ClientValidator
                    val otherClients = existingClients.filter { it.id != client?.id }
                    val validationResult = ClientValidator.validateClient(
                        newClient.toClientSimple(),
                        otherClients.map { it.toClientSimple() }
                    )

                    when (validationResult) {
                        is ValidationResult.Valid -> {
                            onSave(newClient)
                        }
                        is ValidationResult.Invalid -> {
                            validationErrors = validationResult.errors
                        }
                    }
                },
                enabled = !isSaving &&
                        name.isNotBlank() &&
                        price.toDoubleOrNull() != null &&
                        employeePrice.toDoubleOrNull() != null &&
                        companyPrice.toDoubleOrNull() != null
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

// Extension to convert Client to ClientSimple for validation
private fun Client.toClientSimple() = com.payroll.app.desktop.domain.models.ClientSimple(
    id = this.id.toString(),
    name = this.name,
    price = this.price,
    employeePrice = this.employeePrice,
    companyPrice = this.companyPrice,
    employeeId = this.employeeId
)

@Preview
@Composable
fun ClientManagementScreenPreview() {
    PayrollTheme {
        ClientManagementScreen()
    }
}
