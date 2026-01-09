package com.payroll.app.desktop.ui.components.management.employees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.parseHexColor
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.ui.theme.PayrollColors

/**
 * Employee Form Dialog Component
 * Dialog for adding or editing employees
 *
 * Usage:
 * ```kotlin
 * if (showDialog) {
 *     EmployeeFormDialog(
 *         employee = selectedEmployee, // null for add, Employee for edit
 *         isSaving = isLoading,
 *         onDismiss = { viewModel.handleAction(HideDialog) },
 *         onSave = { employee -> viewModel.handleAction(SaveEmployee(employee)) }
 *     )
 * }
 * ```
 */
@Composable
fun EmployeeFormDialog(
    employee: Employee?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit,
    modifier: Modifier = Modifier
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
        title = { Text(if (employee == null) Strings.DialogTitles.addEmployee else Strings.DialogTitles.editEmployee) },
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
                    label = { Text(Strings.EmployeeManagement.employeeName) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    enabled = !isSaving,
                    supportingText = nameError?.let { { Text(it, color = PayrollColors.Error) } }
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(Strings.EmployeeManagement.email) },
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
                    label = { Text(Strings.EmployeeManagement.calendarId) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = calendarIdError != null,
                    enabled = !isSaving,
                    supportingText = calendarIdError?.let { { Text(it, color = PayrollColors.Error) } }
                )

                // Sheet Name
                OutlinedTextField(
                    value = sheetName,
                    onValueChange = { sheetName = it },
                    label = { Text(Strings.EmployeeManagement.sheetName) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )

                // Supervision Price
                OutlinedTextField(
                    value = supervisionPrice,
                    onValueChange = { supervisionPrice = it },
                    label = { Text(Strings.EmployeeManagement.supervisionPrice) },
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
                        label = { Text(Strings.EmployeeManagement.color) },
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
