package com.payroll.app.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payroll.app.desktop.core.network.PayrollApiService
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.presentation.payroll.*
import com.payroll.app.desktop.ui.components.buttons.*
import com.payroll.app.desktop.ui.components.cards.*
import com.payroll.app.desktop.ui.components.dropdowns.*
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import com.payroll.app.desktop.utils.toEuroString
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PayrollScreen(
    modifier: Modifier = Modifier
) {
    val payrollRepository = remember {
        PayrollRepository(PayrollApiService())
    }
    val viewModel = remember {
        PayrollViewModel(payrollRepository)
    }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is PayrollEffect.ShowToast -> {
                    // TODO: Show toast/snackbar
                    println("Toast: ${effect.message}")
                }
                is PayrollEffect.ShowError -> {
                    // TODO: Show error dialog
                    println("Error: ${effect.error}")
                }
                else -> { /* Handle other effects */ }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        PayrollHeader()

        // Main calculation form
        PayrollCalculationForm(
            uiState = uiState,
            onAction = viewModel::handleAction
        )

        // Results section
        uiState.payrollResult?.let { result ->
            PayrollResults(
                result = result,
                onExportPdf = { viewModel.handleAction(PayrollAction.ExportToPdf) },
                onExportExcel = { viewModel.handleAction(PayrollAction.ExportToExcel) }
            )
        }

        // Loading state
        if (uiState.isCalculating) {
            LoadingSection()
        }

        // Error display
        uiState.error?.let { error ->
            ErrorSection(
                error = error,
                onDismiss = { viewModel.handleAction(PayrollAction.ClearError) }
            )
        }
    }
}

@Composable
private fun PayrollHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Σύστημα Μισθοδοσίας",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PayrollColors.Primary
        )
        Text(
            text = "Αυτοματοποιημένος υπολογισμός μισθών από Google Calendar",
            fontSize = 16.sp,
            color = PayrollColors.TextSecondary
        )
        HorizontalDivider(color = PayrollColors.DividerColor)
    }
}

@Composable
private fun PayrollCalculationForm(
    uiState: PayrollState,
    onAction: (PayrollAction) -> Unit
) {
    PayrollCard(
        title = "Υπολογισμός Μισθοδοσίας",
        subtitle = "Επιλέξτε εργαζόμενο και περίοδο"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Employee Selection
            EmployeeDropdown(
                employees = uiState.employees,
                selectedEmployee = uiState.selectedEmployee,
                onEmployeeSelected = { onAction(PayrollAction.SelectEmployee(it)) },
                enabled = !uiState.isLoading && !uiState.isCalculating
            )

            // Date Range Selection
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Από (Ημερομηνία)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PayrollColors.OnSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.startDate?.date?.toString() ?: "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Επιλέξτε ημερομηνία") },
                        enabled = !uiState.isLoading && !uiState.isCalculating
                    )

                    Text(
                        text = "💡 Default: σήμερα - 14 μέρες",
                        fontSize = 12.sp,
                        color = PayrollColors.Info,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Έως (Ημερομηνία)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PayrollColors.OnSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.endDate?.date?.toString() ?: "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Επιλέξτε ημερομηνία") },
                        enabled = !uiState.isLoading && !uiState.isCalculating
                    )
                }
            }

            // Calculate Button
            PayrollButton(
                text = if (uiState.isCalculating) "Υπολογισμός..." else "🔄 Υπολογισμός Μισθοδοσίας",
                onClick = { onAction(PayrollAction.CalculatePayroll) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedEmployee != null &&
                        uiState.startDate != null &&
                        uiState.endDate != null &&
                        !uiState.isCalculating,
                isLoading = uiState.isCalculating,
                type = PayrollButtonType.PRIMARY
            )
        }
    }
}

@Composable
private fun PayrollResults(
    result: com.payroll.app.desktop.domain.models.PayrollResponse,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Summary Cards
        PayrollCard(
            title = "Συγκεντρωτικά Αποτελέσματα"
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Συνολικές Συνεδρίες",
                    value = result.summary.totalSessions.toString(),
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Συνολικά Έσοδα",
                    value = result.summary.totalRevenue.toEuroString(),
                    valueColor = PayrollColors.Success,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Μισθός Εργαζομένου",
                    value = result.summary.employeeEarnings.toEuroString(),
                    valueColor = PayrollColors.Info,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Κέρδη Εταιρίας",
                    value = result.summary.companyEarnings.toEuroString(),
                    valueColor = PayrollColors.Primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Detailed Breakdown
        PayrollCard(
            title = "Αναλυτικά Στοιχεία ανά Πελάτη",
            subtitle = "Crosscheck για επαλήθευση"
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                result.clientBreakdown.forEach { client ->
                    ClientBreakdownItem(client)
                }
            }
        }

        // Export Options
        PayrollCard(
            title = "Εξαγωγή Αποτελεσμάτων"
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PayrollButton(
                    text = "📄 Εξαγωγή PDF",
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    type = PayrollButtonType.SUCCESS
                )

                PayrollButton(
                    text = "📊 Εξαγωγή Excel",
                    onClick = onExportExcel,
                    modifier = Modifier.weight(1f),
                    type = PayrollButtonType.SUCCESS
                )

                PayrollButton(
                    text = "📧 Αποστολή Email",
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f),
                    type = PayrollButtonType.SECONDARY
                )
            }
        }
    }
}

@Composable
private fun ClientBreakdownItem(
    client: com.payroll.app.desktop.domain.models.ClientPayrollDetail
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = PayrollColors.Background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "👤 ${client.clientName}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "${client.sessions} συνεδρίες × €${client.employeePricePerSession}",
                    fontSize = 12.sp,
                    color = PayrollColors.TextSecondary
                )
            }

            Text(
                text = client.employeeEarnings.toEuroString(),
                fontWeight = FontWeight.Bold,
                color = PayrollColors.Success
            )
        }
    }
}

@Composable
private fun LoadingSection() {
    PayrollCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(color = PayrollColors.Primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "🔄 Υπολογισμός σε εξέλιξη...",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Ανάκτηση δεδομένων από Google Calendar",
                    fontSize = 14.sp,
                    color = PayrollColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ErrorSection(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = PayrollColors.Error.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "❌ Σφάλμα",
                    fontWeight = FontWeight.Medium,
                    color = PayrollColors.Error
                )
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = PayrollColors.Error
                )
            }

            TextButton(onClick = onDismiss) {
                Text("Κλείσιμο")
            }
        }
    }
}

// Preview Composables
@Preview
@Composable
private fun PayrollScreenPreview() {
    PayrollTheme {
        Surface {
            val mockState = PayrollState(
                employees = listOf(
                    Employee(
                        id = "1",
                        name = "Αγγελική Γκουντοπούλου",
                        email = "psy.gkountopoulou@gmail.com",
                        calendarId = "calendar1"
                    )
                ),
                selectedEmployee = null,
                startDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            PayrollCalculationForm(
                uiState = mockState,
                onAction = { }
            )
        }
    }
}

@Preview
@Composable
private fun PayrollHeaderPreview() {
    PayrollTheme {
        Surface {
            PayrollHeader()
        }
    }
}

@Preview
@Composable
private fun LoadingSectionPreview() {
    PayrollTheme {
        Surface {
            LoadingSection()
        }
    }
}