package com.payroll.app.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.payroll.app.desktop.domain.models.ClientSimple
import com.payroll.app.desktop.domain.models.DummyData
import com.payroll.app.desktop.domain.models.EmployeeSimple
import com.payroll.app.desktop.domain.usecases.ClientUseCases
import com.payroll.app.desktop.domain.usecases.ClientValidationException
import com.payroll.app.desktop.domain.validation.ValidationError
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.collections.find
import kotlin.time.Clock.System

@Composable
fun ClientManagementScreen() {
    var selectedEmployeeId by remember { mutableStateOf(DummyData.employees.firstOrNull()?.id) }
    var searchQuery by remember { mutableStateOf("") }

    // Mutable list για local state management
    var clientsList by remember { mutableStateOf(DummyData.clients) }

    val filteredEmployees = DummyData.employees.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val selectedEmployee = DummyData.employees.find { it.id == selectedEmployeeId }
    val employeeClients = clientsList.filter { it.employeeId == selectedEmployeeId }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PayrollColors.Background
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT PANEL - Employees
            EmployeeListPanel(
                employees = filteredEmployees,
                selectedEmployeeId = selectedEmployeeId,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onEmployeeSelected = { selectedEmployeeId = it }
            )

            // DIVIDER
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = PayrollColors.DividerColor
            )

            // RIGHT PANEL - Clients
            ClientTablePanel(
                employee = selectedEmployee,
                clients = employeeClients,
                onAddClient = { newClient ->
                    clientsList = clientsList + newClient
                },
                onEditClient = { updatedClient ->
                    clientsList = clientsList.map {
                        if (it.id == updatedClient.id) updatedClient else it
                    }
                },
                onDeleteClient = { clientId ->
                    clientsList = clientsList.filter { it.id != clientId }
                }
            )
        }
    }
}

@Composable
fun EmployeeListPanel(
    employees: List<EmployeeSimple>,
    selectedEmployeeId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEmployeeSelected: (String) -> Unit
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
            text = "👥 Εργαζόμενοι",
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
            placeholder = { Text("🔍 Αναζήτηση...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PayrollColors.Primary,
                unfocusedBorderColor = PayrollColors.DividerColor
            )
        )

        // Employee List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(employees) { employee ->
                EmployeeCard(
                    employee = employee,
                    isSelected = employee.id == selectedEmployeeId,
                    onClick = { onEmployeeSelected(employee.id) }
                )
            }
        }
    }
}

@Composable
fun EmployeeCard(
    employee: EmployeeSimple,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PayrollColors.CardBackground else PayrollColors.CardBackground
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
                Text(
                    text = employee.email,
                    fontSize = 12.sp,
                    color = PayrollColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Client count badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PayrollColors.Primary else PayrollColors.Primary
            ) {
                Text(
                    text = "${employee.clientCount}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else PayrollColors.Primary
                )
            }
        }
    }
}

@Composable
fun ClientTablePanel(
    employee: EmployeeSimple?,
    clients: List<ClientSimple>,
    onAddClient: (ClientSimple) -> Unit,
    onEditClient: (ClientSimple) -> Unit,
    onDeleteClient: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingClient by remember { mutableStateOf<ClientSimple?>(null) }
    var deleteConfirmClient by remember { mutableStateOf<ClientSimple?>(null) }

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
                    text = "📋 Πελάτες",
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
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PayrollColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Προσθήκη Πελάτη")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Table
        if (clients.isEmpty()) {
            EmptyClientsView()
        } else {
            ClientsTable(
                clients = clients,
                onEditClient = { editingClient = it },
                onDeleteClient = { deleteConfirmClient = it }
            )
        }
    }

    // Add Dialog
    if (showAddDialog && employee != null) {
        ClientFormDialog(
            client = null,
            employeeId = employee.id,
            existingClients = clients, // 🆕 PASS THIS
            onDismiss = { showAddDialog = false },
            onSave = { newClient ->
                onAddClient(newClient)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    editingClient?.let { client ->
        ClientFormDialog(
            client = client,
            employeeId = employee?.id ?: "",
            existingClients = clients, // 🆕 PASS THIS
            onDismiss = { editingClient = null },
            onSave = { updatedClient ->
                onEditClient(updatedClient)
                editingClient = null
            }
        )
    }

    // Delete Confirmation
    deleteConfirmClient?.let { client ->
        AlertDialog(
            onDismissRequest = { deleteConfirmClient = null },
            title = { Text("Διαγραφή Πελάτη") },
            text = { Text("Είστε σίγουροι ότι θέλετε να διαγράψετε τον πελάτη '${client.name}';") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClient(client.id)
                        deleteConfirmClient = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = PayrollColors.Error
                    )
                ) {
                    Text("Διαγραφή")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmClient = null }) {
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
        }
    }
}

@Composable
fun ClientsTable(
    clients: List<ClientSimple>,
    onEditClient: (ClientSimple) -> Unit,
    onDeleteClient: (ClientSimple) -> Unit
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
                    .background(PayrollColors.Primary)
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

            Divider(color = PayrollColors.DividerColor)

            // Table Rows
            LazyColumn {
                items(clients) { client ->
                    ClientRow(
                        client = client,
                        onEdit = { onEditClient(client) },
                        onDelete = { onDeleteClient(client) }
                    )
                    Divider(color = PayrollColors.DividerColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun ClientRow(
    client: ClientSimple,
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
            "${client.price}€",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PayrollColors.Success
        )
        Text(
            "${client.employeePrice}€",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = PayrollColors.Info
        )
        Text(
            "${client.companyPrice}€",
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
    client: ClientSimple?,
    employeeId: String,
    existingClients: List<ClientSimple>,
    onDismiss: () -> Unit,
    onSave: (ClientSimple) -> Unit
) {
    // 🔄 ΣΩΣΤΗ ΣΕΙΡΑ: Πρώτα η τιμή πελάτη!
    var price by remember { mutableStateOf(client?.price?.toString() ?: "") }
    var employeePrice by remember { mutableStateOf(client?.employeePrice?.toString() ?: "") }
    var companyPrice by remember { mutableStateOf(client?.companyPrice?.toString() ?: "") }
    var name by remember { mutableStateOf(client?.name ?: "") }

    var validationErrors by remember { mutableStateOf<List<ValidationError>>(emptyList()) }
    val clientUseCases = remember { ClientUseCases() }

    // 🆕 Auto-calculate company price when total or employee changes
    var autoCalculateCompany by remember { mutableStateOf(true) }

    LaunchedEffect(price, employeePrice) {
        if (autoCalculateCompany && price.isNotBlank() && employeePrice.isNotBlank()) {
            val totalPrice = price.toDoubleOrNull() ?: 0.0
            val empPrice = employeePrice.toDoubleOrNull() ?: 0.0
            val calculated = clientUseCases.calculateCompanyPrice(totalPrice, empPrice)
            companyPrice = if (calculated > 0) calculated.toString() else ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                        validationErrors = emptyList()
                    },
                    label = { Text("Όνομα Πελάτη *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == "name" },
                    supportingText = {
                        validationErrors.find { it.field == "name" }?.let {
                            Text(it.message, color = PayrollColors.Error)
                        }
                    }
                )

                // 🔄 ΠΡΩΤΑ: Total Price (η βάση!)
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        validationErrors = emptyList()
                    },
                    label = { Text("Τιμή Πελάτη (€) * [ΒΑΣΗ]") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == "price" },
                    supportingText = {
                        validationErrors.find { it.field == "price" }?.let {
                            Text(it.message, color = PayrollColors.Error)
                        }
                    }
                )

                Divider(color = PayrollColors.DividerColor)

                // Employee Price
                OutlinedTextField(
                    value = employeePrice,
                    onValueChange = {
                        employeePrice = it
                        validationErrors = emptyList()
                    },
                    label = { Text("Τιμή Εργαζόμενου (€) *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == "employeePrice" },
                    supportingText = {
                        validationErrors.find { it.field == "employeePrice" }?.let {
                            Text(it.message, color = PayrollColors.Error)
                        }
                    }
                )

                // Company Price (with auto-calculate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = companyPrice,
                        onValueChange = {
                            companyPrice = it
                            autoCalculateCompany = false // Disable auto if manually edited
                            validationErrors = emptyList()
                        },
                        label = { Text("Τιμή Εταιρίας (€) *") },
                        modifier = Modifier.weight(1f),
                        isError = validationErrors.any { it.field == "companyPrice" },
                        supportingText = {
                            validationErrors.find { it.field == "companyPrice" }?.let {
                                Text(it.message, color = PayrollColors.Error)
                            }
                        }
                    )

                    // Auto-calculate button
                    IconButton(
                        onClick = {
                            autoCalculateCompany = true
                            val totalPrice = price.toDoubleOrNull() ?: 0.0
                            val empPrice = employeePrice.toDoubleOrNull() ?: 0.0
                            companyPrice = clientUseCases.calculateCompanyPrice(totalPrice, empPrice).toString()
                        }
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Auto-calculate company price",
                            tint = if (autoCalculateCompany) PayrollColors.Primary else PayrollColors.TextSecondary
                        )
                    }
                }

                // 🆕 Real-time validation hint
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
                                    text = "Άθροισμα: ${empPrice}€ + ${compPrice}€ = ${sum}€",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (diff <= 0.01) PayrollColors.Success else PayrollColors.Warning
                                )
                                if (diff > 0.01) {
                                    Text(
                                        text = "Αναμενόμενο: ${totalPrice}€ (διαφορά: ${diff}€)",
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
                    val newClient = ClientSimple(
                        id = client?.id ?: "c${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}",
                        name = name.trim(),
                        price = price.toDoubleOrNull() ?: 0.0,
                        employeePrice = employeePrice.toDoubleOrNull() ?: 0.0,
                        companyPrice = companyPrice.toDoubleOrNull() ?: 0.0,
                        employeeId = employeeId
                    )

                    val result = if (client == null) {
                        clientUseCases.createClient(newClient, existingClients)
                    } else {
                        clientUseCases.updateClient(newClient, existingClients)
                    }

                    result.fold(
                        onSuccess = { validClient ->
                            onSave(validClient)
                        },
                        onFailure = { exception ->
                            if (exception is ClientValidationException) {
                                validationErrors = exception.errors
                            }
                        }
                    )
                },
                enabled = name.isNotBlank() &&
                        price.toDoubleOrNull() != null &&
                        employeePrice.toDoubleOrNull() != null &&
                        companyPrice.toDoubleOrNull() != null
            ) {
                Text("Αποθήκευση")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ακύρωση")
            }
        }
    )
}

@Preview
@Composable
fun ClientManagementScreenPreview() {
    PayrollTheme {
        ClientManagementScreen()
    }
}