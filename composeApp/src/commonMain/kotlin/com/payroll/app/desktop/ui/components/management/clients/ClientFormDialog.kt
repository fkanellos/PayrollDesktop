package com.payroll.app.desktop.ui.components.management.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.domain.usecases.ClientUseCases
import com.payroll.app.desktop.domain.validation.*
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.utils.toEuroString

// Field name constants
private object ClientFormFields {
    const val NAME = "name"
    const val PRICE = "price"
    const val EMPLOYEE_PRICE = "employeePrice"
    const val COMPANY_PRICE = "companyPrice"
}

/**
 * Client Form Dialog Component
 * Dialog for adding or editing clients with price validation and auto-calculation
 *
 * Usage:
 * ```kotlin
 * if (showDialog) {
 *     ClientFormDialog(
 *         client = selectedClient, // null for add, Client for edit
 *         employeeId = selectedEmployeeId,
 *         existingClients = allClients,
 *         isSaving = isLoading,
 *         onDismiss = { viewModel.handleAction(HideDialog) },
 *         onSave = { client -> viewModel.handleAction(SaveClient(client)) }
 *     )
 * }
 * ```
 */
@Composable
fun ClientFormDialog(
    client: Client?,
    employeeId: String,
    existingClients: List<Client>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Client) -> Unit,
    modifier: Modifier = Modifier
) {
    var price by remember { mutableStateOf(client?.price?.toString() ?: "") }
    var employeePrice by remember { mutableStateOf(client?.employeePrice?.toString() ?: "") }
    var companyPrice by remember { mutableStateOf(client?.companyPrice?.toString() ?: "") }
    var name by remember { mutableStateOf(client?.name ?: "") }

    var validationErrors by remember { mutableStateOf<List<ValidationError>>(emptyList()) }
    val clientUseCases = remember { ClientUseCases() }

    // Auto-calculate company price (enabled by default for both add and edit)
    var autoCalculateCompany by remember { mutableStateOf(true) }

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
        title = { Text(if (client == null) Strings.DialogTitles.addClient else Strings.DialogTitles.editClient) },
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
                        validationErrors = validationErrors.filter { e -> e.field != ClientFormFields.NAME }
                    },
                    label = { Text(Strings.ClientManagement.clientName) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == ClientFormFields.NAME },
                    enabled = !isSaving,
                    supportingText = {
                        validationErrors.find { it.field == ClientFormFields.NAME }?.let {
                            Text(it.message, color = PayrollColors.Error)
                        }
                    }
                )

                // Total Price
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        validationErrors = validationErrors.filter { e -> e.field != ClientFormFields.PRICE }
                    },
                    label = { Text(Strings.ClientManagement.clientPrice) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == ClientFormFields.PRICE },
                    enabled = !isSaving,
                    supportingText = {
                        validationErrors.find { it.field == ClientFormFields.PRICE }?.let {
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
                        validationErrors = validationErrors.filter { e -> e.field != ClientFormFields.EMPLOYEE_PRICE }
                    },
                    label = { Text(Strings.ClientManagement.employeePrice) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationErrors.any { it.field == ClientFormFields.EMPLOYEE_PRICE },
                    enabled = !isSaving,
                    supportingText = {
                        validationErrors.find { it.field == ClientFormFields.EMPLOYEE_PRICE }?.let {
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
                            validationErrors = validationErrors.filter { e -> e.field != ClientFormFields.COMPANY_PRICE }
                        },
                        label = { Text(Strings.ClientManagement.companyPrice) },
                        modifier = Modifier.weight(1f),
                        isError = validationErrors.any { it.field == ClientFormFields.COMPANY_PRICE },
                        enabled = !isSaving,
                        supportingText = {
                            validationErrors.find { it.field == ClientFormFields.COMPANY_PRICE }?.let {
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
                Text(if (isSaving) Strings.Common.loading else Strings.Common.save)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(Strings.Common.cancel)
            }
        },
        modifier = modifier
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
