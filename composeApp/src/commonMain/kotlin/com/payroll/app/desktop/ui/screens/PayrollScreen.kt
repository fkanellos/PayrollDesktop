package com.payroll.app.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
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

        // Results section με enhanced client breakdown
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

    // Date Pickers
    if (uiState.showStartDatePicker) {
        DatePickerDialog(
            title = "Επιλογή Ημερομηνίας Έναρξης",
            onDateSelected = { date ->
                viewModel.handleAction(PayrollAction.SetStartDate(date))
                viewModel.handleAction(PayrollAction.HideStartDatePicker)
            },
            onDismiss = { viewModel.handleAction(PayrollAction.HideStartDatePicker) }
        )
    }

    if (uiState.showEndDatePicker) {
        DatePickerDialog(
            title = "Επιλογή Ημερομηνίας Λήξης",
            onDateSelected = { date ->
                viewModel.handleAction(PayrollAction.SetEndDate(date))
                viewModel.handleAction(PayrollAction.HideEndDatePicker)
            },
            onDismiss = { viewModel.handleAction(PayrollAction.HideEndDatePicker) }
        )
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

            // Selected Employee Info Card
            uiState.selectedEmployee?.let { employee ->
                SelectedEmployeeCard(employee = employee)
            }

            // Date Range Selection με Calendar Pickers
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalendarDateField(
                    modifier = Modifier.weight(1f),
                    label = "Από (Ημερομηνία)",
                    date = uiState.startDate,
                    onClick = { onAction(PayrollAction.ShowStartDatePicker) },
                    enabled = !uiState.isLoading && !uiState.isCalculating
                )

                CalendarDateField(
                    modifier = Modifier.weight(1f),
                    label = "Έως (Ημερομηνία)",
                    date = uiState.endDate,
                    onClick = { onAction(PayrollAction.ShowEndDatePicker) },
                    enabled = !uiState.isLoading && !uiState.isCalculating
                )
            }

            // Helper text
            Text(
                text = "💡 Default: σήμερα - 14 μέρες",
                fontSize = 12.sp,
                color = PayrollColors.Info
            )

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
private fun SelectedEmployeeCard(employee: Employee) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = PayrollColors.Primary.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, PayrollColors.Primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = PayrollColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Επιλεγμένος: ${employee.name}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = PayrollColors.Primary
                )
                Text(
                    text = "📧 ${employee.email}",
                    fontSize = 14.sp,
                    color = PayrollColors.TextSecondary
                )
                Text(
                    text = "🗓️ Calendar: ${employee.calendarId.take(25)}...",
                    fontSize = 12.sp,
                    color = PayrollColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CalendarDateField(
    modifier: Modifier = Modifier,
    label: String,
    date: kotlinx.datetime.LocalDateTime?,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PayrollColors.OnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = if (enabled) onClick else { {} },
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) PayrollColors.Surface else PayrollColors.Surface.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, PayrollColors.DividerColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = PayrollColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = date?.date?.toString() ?: "Επιλέξτε ημερομηνία",
                        fontSize = 14.sp,
                        color = if (date != null) PayrollColors.OnSurface else PayrollColors.TextSecondary
                    )
                }

                if (enabled) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Επιλογή ημερομηνίας",
                        tint = PayrollColors.Primary
                    )
                }
            }
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

        // Enhanced Detailed Breakdown - Λίστα με πελάτες και αναλυτικά
        PayrollCard(
            title = "Αναλυτικά Στοιχεία ανά Πελάτη",
            subtitle = "Λεπτομερής ανάλυση συνεδριών και εσόδων"
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                result.clientBreakdown.forEach { client ->
                    EnhancedClientBreakdownItem(client)
                }

                if (result.clientBreakdown.isEmpty()) {
                    EmptyClientBreakdownCard()
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
private fun EnhancedClientBreakdownItem(
    client: com.payroll.app.desktop.domain.models.ClientPayrollDetail
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) PayrollColors.Primary.copy(alpha = 0.05f) else PayrollColors.CardBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (expanded) PayrollColors.Primary.copy(alpha = 0.3f) else PayrollColors.DividerColor
        )
    ) {
        Column {
            // Main client info - clickable για expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = PayrollColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = client.clientName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${client.sessions} συνεδρίες × ${client.employeePricePerSession.toEuroString()} = ${client.employeeEarnings.toEuroString()}",
                        fontSize = 13.sp,
                        color = PayrollColors.Success,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Συνολικά έσοδα πελάτη: ${client.totalRevenue.toEuroString()}",
                        fontSize = 12.sp,
                        color = PayrollColors.TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = client.employeeEarnings.toEuroString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PayrollColors.Success
                    )

                    Text(
                        text = "Μερίδιο εργαζομένου",
                        fontSize = 11.sp,
                        color = PayrollColors.TextSecondary
                    )
                }

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Σύμπτυξη" else "Επέκταση",
                    tint = PayrollColors.Primary
                )
            }

            // Expanded details
            if (expanded) {
                HorizontalDivider(color = PayrollColors.DividerColor)

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Financial breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FinancialItem(
                            label = "Τιμή/Συνεδρία",
                            value = client.pricePerSession.toEuroString(),
                            color = PayrollColors.OnSurface
                        )
                        FinancialItem(
                            label = "Εργαζόμενος",
                            value = client.employeePricePerSession.toEuroString(),
                            color = PayrollColors.Success
                        )
                        FinancialItem(
                            label = "Εταιρία",
                            value = client.companyPricePerSession.toEuroString(),
                            color = PayrollColors.Warning
                        )
                    }

                    // Session details αν υπάρχουν
                    if (client.eventDetails.isNotEmpty()) {
                        Text(
                            text = "📅 Συνεδρίες (${client.eventDetails.size})",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = PayrollColors.OnSurface
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            client.eventDetails.take(3).forEach { event -> // Show first 3
                                SessionItem(event)
                            }

                            if (client.eventDetails.size > 3) {
                                Text(
                                    text = "... και ${client.eventDetails.size - 3} ακόμη συνεδρίες",
                                    fontSize = 12.sp,
                                    color = PayrollColors.TextSecondary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = PayrollColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SessionItem(event: com.payroll.app.desktop.domain.models.EventDetail) {
    val statusIcon = when (event.status) {
        "completed" -> "✅"
        "cancelled" -> "❌"
        "pending_payment" -> "⏳"
        else -> "📅"
    }

    val statusColor = when (event.status) {
        "completed" -> PayrollColors.Success
        "cancelled" -> PayrollColors.Error
        "pending_payment" -> PayrollColors.Warning
        else -> PayrollColors.TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = statusIcon, fontSize = 12.sp)
            Text(
                text = "${event.date} - ${event.time}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = event.duration,
            fontSize = 11.sp,
            color = PayrollColors.TextSecondary
        )
    }
}

@Composable
private fun EmptyClientBreakdownCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = PayrollColors.Warning.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = PayrollColors.Warning,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Δεν βρέθηκαν συνεδρίες",
                fontWeight = FontWeight.Medium,
                color = PayrollColors.Warning
            )
            Text(
                text = "Ελέγξτε αν υπάρχουν εγγραφές στο Google Calendar για την επιλεγμένη περίοδο",
                fontSize = 14.sp,
                color = PayrollColors.TextSecondary,
                textAlign = TextAlign.Center
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    title: String,
    onDateSelected: (kotlinx.datetime.LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                        onDateSelected(localDateTime)
                    }
                }
            ) {
                Text("Επιλογή")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ακύρωση")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
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