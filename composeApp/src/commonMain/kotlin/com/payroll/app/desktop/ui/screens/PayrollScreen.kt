@file:OptIn(ExperimentalMaterial3Api::class)

package com.payroll.app.desktop.ui.screens

import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.core.resources.toDisplayString
import com.payroll.app.desktop.core.strings.Strings
import com.payroll.app.desktop.core.utils.format
import com.payroll.app.desktop.ui.components.shared.errors.ErrorBanner
import com.payroll.app.desktop.ui.components.shared.loading.LoadingIndicator
import com.payroll.app.desktop.ui.components.payroll.events.UnmatchedEventsCard
import com.payroll.app.desktop.ui.components.payroll.results.ClientBreakdownCard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.core.constants.AppConstants
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.presentation.payroll.*
import com.payroll.app.desktop.ui.components.buttons.*
import com.payroll.app.desktop.ui.components.cards.*
import com.payroll.app.desktop.ui.components.dropdowns.*
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import com.payroll.app.desktop.utils.DateRanges
import com.payroll.app.desktop.utils.toEpochMillis
import com.payroll.app.desktop.utils.toEuroString
import com.payroll.app.desktop.utils.toGreekDateString
import com.payroll.app.desktop.utils.toLocalDate
import com.payroll.app.desktop.utils.toReadableString
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollScreen(
    modifier: Modifier = Modifier,
    viewModel: PayrollViewModel = koinInject()
) {

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🆕 State για confirmation dialog
    var showSheetsConfirmation by remember { mutableStateOf(false) }
    var sheetsConfirmationData by remember {
        mutableStateOf<Triple<String, String, Boolean>?>(null)
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is PayrollEffect.ShowToast -> {
                    // 🔥 NEW: Format message in UI layer
                    val message = effect.message.toDisplayString()
                    Logger.debug("UI", "Toast: $message")
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
                is PayrollEffect.ShowError -> {
                    // Error dialog handled via state (showErrorDialog)
                    val message = effect.message.toDisplayString()
                    Logger.debug("UI", "Error: $message")
                }
                // 🆕 Handle confirmation request
                is PayrollEffect.RequestSheetsConfirmation -> {
                    sheetsConfirmationData = Triple(
                        effect.payrollId,
                        effect.message,
                        effect.isUpdate
                    )
                    showSheetsConfirmation = true
                }
                else -> { /* Handle other effects */ }
            }
        }
    }

    // Scaffold for Snackbar support
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with Sync and Refresh buttons
            item {
                PayrollHeader(
                    isLoading = uiState.isLoading,
                    onRefreshClick = { viewModel.handleAction(PayrollAction.RefreshData) }
                )
            }

            // Main calculation form
            item {
                PayrollCalculationForm(
                    uiState = uiState,
                    onAction = viewModel::handleAction
                )
            }

            // Results section με enhanced client breakdown
            uiState.payrollResult?.let { result ->
                item {
                    PayrollResults(
                        result = result,
                        onExportPdf = { viewModel.handleAction(PayrollAction.ExportToPdf) },
                        onExportExcel = { viewModel.handleAction(PayrollAction.ExportToExcel) },
                        onAddClient = { originalTitle, editedName, price, empPrice, compPrice ->
                            viewModel.handleAction(
                                PayrollAction.AddUnmatchedClient(
                                    originalEventTitle = originalTitle,
                                    name = editedName,
                                    price = price,
                                    employeePrice = empPrice,
                                    companyPrice = compPrice
                                )
                            )
                        },
                        addedClients = uiState.addedClients
                    )
                }
            }

            // Loading state
            if (uiState.isCalculating) {
                item {
                    LoadingIndicator(
                        message = "Υπολογισμός σε εξέλιξη... Ανάκτηση δεδομένων από Google Calendar"
                    )
                }
            }

            // Error display
            uiState.error?.let { error ->
                item {
                    ErrorBanner(
                        message = error,
                        onDismiss = { viewModel.handleAction(PayrollAction.ClearError) }
                    )
                }
            }
        }
    }

    // Date Pickers
    if (uiState.showStartDatePicker) {
        DatePickerDialog(
            title = Strings.Payroll.startDate,
            initialDate = uiState.startDate, // 🔧 ΠΡΟΣΘΗΚΗ: Προεπιλεγμένη ημερομηνία
            onDateSelected = { date ->
                viewModel.handleAction(PayrollAction.SetStartDate(date))
                viewModel.handleAction(PayrollAction.HideStartDatePicker)
            },
            onDismiss = { viewModel.handleAction(PayrollAction.HideStartDatePicker) }
        )
    }

    if (uiState.showEndDatePicker) {
        DatePickerDialog(
            title = Strings.Payroll.endDate,
            initialDate = uiState.endDate, // 🔧 ΠΡΟΣΘΗΚΗ: Προεπιλεγμένη ημερομηνία
            onDateSelected = { date ->
                viewModel.handleAction(PayrollAction.SetEndDate(date))
                viewModel.handleAction(PayrollAction.HideEndDatePicker)
            },
            onDismiss = { viewModel.handleAction(PayrollAction.HideEndDatePicker) }
        )
    }
    // 🆕 Sheets Confirmation Dialog
    if (showSheetsConfirmation) {
        sheetsConfirmationData?.let { (payrollId, message, isUpdate) ->
            SheetsConfirmationDialog(
                message = message,
                isUpdate = isUpdate,
                onConfirm = {
                    viewModel.confirmAndSyncToSheets(payrollId)
                    showSheetsConfirmation = false
                    sheetsConfirmationData = null
                },
                onDismiss = {
                    showSheetsConfirmation = false
                    sheetsConfirmationData = null
                }
            )
        }
    }

    // 🆕 Match Confirmation Dialog (Paginated)
    if (uiState.uncertainMatches.isNotEmpty()) {
        com.payroll.app.desktop.ui.components.dialogs.MatchConfirmationPaginatedDialog(
            uncertainMatches = uiState.uncertainMatches,
            onConfirm = { match ->
                viewModel.handleAction(PayrollAction.ConfirmMatch(match))
            },
            onReject = { match ->
                viewModel.handleAction(PayrollAction.RejectMatch(match))
            },
            onDismiss = {
                viewModel.handleAction(PayrollAction.ClearUncertainMatches)
            }
        )
    }

    // Error Dialog
    if (uiState.showErrorDialog) {
        uiState.errorDialogMessage?.let { errorMessage ->
            com.payroll.app.desktop.ui.components.ErrorDialog(
                title = Strings.Common.error,
                message = errorMessage,
                severity = com.payroll.app.desktop.ui.components.ErrorSeverity.ERROR,
                onDismiss = { viewModel.handleAction(PayrollAction.DismissErrorDialog) }
            )
        }
    }
}
/**
 * 🆕 Confirmation Dialog για Google Sheets sync
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetsConfirmationDialog(
    message: String,
    isUpdate: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (isUpdate) Icons.Default.Update else Icons.Default.Upload,
                contentDescription = null,
                tint = if (isUpdate) PayrollColors.Warning else PayrollColors.Success,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = if (isUpdate) "⚠️ Ενημέρωση Sheets" else "📤 Αποστολή σε Sheets",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (isUpdate) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = PayrollColors.Warning.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, PayrollColors.Warning.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = PayrollColors.Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Τα παλιά δεδομένα θα διαγραφούν και θα αντικατασταθούν.",
                                fontSize = 12.sp,
                                color = PayrollColors.Warning
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUpdate) PayrollColors.Warning else PayrollColors.Success
                )
            ) {
                Icon(
                    if (isUpdate) Icons.Default.Update else Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isUpdate) Strings.Common.save else Strings.Common.save)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Strings.Common.cancel)
            }
        }
    )
}
@Composable
private fun PayrollHeader(
    isLoading: Boolean = false,
    onRefreshClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.Payroll.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayrollColors.Primary
                )
                Text(
                    text = Strings.Payroll.subtitle,
                    fontSize = 16.sp,
                    color = PayrollColors.TextSecondary
                )
            }

            // Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Refresh Button
                OutlinedButton(
                    onClick = onRefreshClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PayrollColors.Info
                    ),
                    border = BorderStroke(1.dp, PayrollColors.Info)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = PayrollColors.Info
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.Common.refreshing)
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = Strings.Common.refresh,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.Common.refresh)
                    }
                }
            }
        }
        HorizontalDivider(color = PayrollColors.DividerColor)
    }
}

// Updated PayrollCalculationForm με collapsible date selection

@Composable
private fun PayrollCalculationForm(
    uiState: PayrollState,
    onAction: (PayrollAction) -> Unit
) {
    var showCustomDates by remember { mutableStateOf(false) }

    PayrollCard(
        title = Strings.Payroll.title,
        subtitle = Strings.Payroll.selectEmployee
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Employee Selection
            EnhancedEmployeeDropdown(
                employees = uiState.employees,
                selectedEmployee = uiState.selectedEmployee,
                onEmployeeSelected = { onAction(PayrollAction.SelectEmployee(it)) },
                enabled = !uiState.isLoading && !uiState.isCalculating
            )

            // Quick Period Selection
            PredefinedPeriodDropdown(
                currentStartDate = uiState.startDate,
                currentEndDate = uiState.endDate,
                onPeriodSelected = { (start, end) ->
                    onAction(PayrollAction.SetStartDate(start))
                    onAction(PayrollAction.SetEndDate(end))
                    // Όταν επιλέγει από dropdown, κλείνουμε τις custom dates
                    showCustomDates = false
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Toggle για Custom Dates
            CustomDateToggle(
                isExpanded = showCustomDates,
                onToggle = { showCustomDates = !showCustomDates },
                hasCustomDates = uiState.startDate != null && uiState.endDate != null
            )

            // Collapsible Custom Date Selection
            AnimatedVisibility(
                visible = showCustomDates,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "📅 Προσαρμοσμένες Ημερομηνίες",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PayrollColors.Primary
                    )

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

                    // Helper text για custom dates
                    Text(
                        text = "💡 Επιλέξτε συγκεκριμένες ημερομηνίες για προσαρμοσμένη περίοδο",
                        fontSize = 12.sp,
                        color = PayrollColors.Info
                    )
                }
            }

            // Calculate Button
            PayrollButton(
                text = if (uiState.isCalculating) Strings.Payroll.calculating else "🔄 ${Strings.Payroll.calculate}",
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
private fun CustomDateToggle(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    hasCustomDates: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) PayrollColors.Primary.copy(alpha = 0.1f) else PayrollColors.Surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) PayrollColors.Primary.copy(alpha = 0.3f) else PayrollColors.DividerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isExpanded) Icons.Default.CalendarViewMonth else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = PayrollColors.Primary,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = "Προσαρμοσμένες Ημερομηνίες",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = PayrollColors.OnSurface
                    )

                    if (hasCustomDates && !isExpanded) {
                        Text(
                            text = "Ενεργές",
                            fontSize = 12.sp,
                            color = PayrollColors.Success
                        )
                    } else if (!hasCustomDates) {
                        Text(
                            text = "Επιλέξτε συγκεκριμένες ημερομηνίες",
                            fontSize = 12.sp,
                            color = PayrollColors.TextSecondary
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasCustomDates && !isExpanded) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Ενεργές",
                        tint = PayrollColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Σύμπτυξη" else "Επέκταση",
                    tint = PayrollColors.Primary
                )
            }
        }
    }
}

@Composable
fun EnhancedEmployeeDropdown(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    onEmployeeSelected: (Employee) -> Unit,
    modifier: Modifier = Modifier,
    label: String = Strings.Payroll.selectEmployee,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    // 🔧 Debug και fallback για το display value
    val displayValue = when {
        selectedEmployee?.name?.isNotBlank() == true -> selectedEmployee.name
        selectedEmployee != null -> "Εργαζόμενος (ID: ${selectedEmployee.id})"
        else -> ""
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PayrollColors.OnPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (enabled) expanded = !expanded
            }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                value = displayValue,
                onValueChange = {},
                placeholder = if (selectedEmployee == null) {
                    {
                        Text(
                            Strings.Payroll.selectEmployeePlaceholder,
                            color = PayrollColors.TextSecondary
                        )
                    }
                } else null,
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (selectedEmployee != null) PayrollColors.PrimaryVariant else PayrollColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow",
                        tint = PayrollColors.Primary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PayrollColors.Primary,
                    unfocusedBorderColor = PayrollColors.DividerColor,
                    disabledBorderColor = PayrollColors.DividerColor.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = enabled,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = if (selectedEmployee != null) PayrollColors.OnSurface else PayrollColors.TextSecondary
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (employees.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                Strings.Payroll.noEmployees,
                                color = PayrollColors.TextSecondary
                            )
                        },
                        onClick = { }
                    )
                } else {
                    employees.forEach { employee ->
                        val isSelected = selectedEmployee?.id == employee.id

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = employee.name.ifBlank { "Εργαζόμενος ${employee.id}" },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (isSelected) PayrollColors.Primary else PayrollColors.PrimaryVariant
                                        )
                                        if (employee.email.isNotBlank()) {
                                            Text(
                                                text = "📧 ${employee.email}",
                                                fontSize = 12.sp,
                                                color = PayrollColors.TextSecondary
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Επιλεγμένος",
                                            tint = PayrollColors.Primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onEmployeeSelected(employee)
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = if (isSelected) PayrollColors.Primary else PayrollColors.PrimaryVariant
                            )
                        )
                    }
                }
            }
        }

        // Status indicator βάσει της επιλογής
        selectedEmployee?.let { employee ->
            Text(
                text = "✅ Επιλεγμένος: ${employee.name.ifBlank { "Εργαζόμενος ${employee.id}" }}",
                fontSize = 12.sp,
                color = PayrollColors.Success,
                modifier = Modifier.padding(top = 4.dp)
            )
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
                        // 🔧 FIX: Χρησιμοποιούμε το σωστό format dd/mm/yyyy
                        text = date?.toGreekDateString() ?: "Επιλέξτε ημερομηνία",
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
    onExportExcel: () -> Unit,
    onNavigateToClients: () -> Unit = {},
    onAddClient: (originalTitle: String, editedName: String, price: Double, employeePrice: Double, companyPrice: Double) -> Unit = { _, _, _, _, _ -> },
    addedClients: Set<String> = emptySet()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Summary Cards
        PayrollCard(
            title = Strings.Payroll.results
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = Strings.Payroll.totalSessions,
                    value = result.summary.totalSessions.toString(),
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = Strings.Payroll.totalRevenue,
                    value = result.summary.totalRevenue.toEuroString(),
                    valueColor = PayrollColors.Success,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = Strings.Payroll.employeeEarnings,
                    value = result.summary.employeeEarnings.toEuroString(),
                    valueColor = PayrollColors.Info,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = Strings.Payroll.companyEarnings,
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
            if (result.clientBreakdown.isEmpty()) {
                EmptyClientBreakdownCard()
            } else {
                // 🚀 PERFORMANCE: LazyColumn with itemsIndexed() for efficient rendering of large lists
                LazyColumn(
                    modifier = Modifier.heightIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = result.clientBreakdown,
                        key = { index, client -> "$index-${client.clientName}" } // Composite key (index + name) for uniqueness
                    ) { _, client ->
                        ClientBreakdownCard(client)
                    }
                }
            }
        }

        // Unmatched Events Section (if any)
        result.eventTracking?.unmatchedEvents?.let { unmatchedEvents ->
            if (unmatchedEvents.isNotEmpty() || addedClients.isNotEmpty()) {
                UnmatchedEventsCard(
                    unmatchedEvents = unmatchedEvents,
                    addedClients = addedClients,
                    onManageClients = onNavigateToClients,
                    onAddClient = onAddClient
                )
            }
        }

        // Export Options
        PayrollCard(
            title = Strings.Payroll.export
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
                    onClick = {
                        Logger.info("PayrollScreen", "Email feature not yet implemented")
                    },
                    modifier = Modifier.weight(1f),
                    type = PayrollButtonType.SECONDARY,
                    enabled = false  // Not implemented yet
                )
            }
        }
    }
}

// EnhancedClientBreakdownItem and helper components extracted to ClientBreakdownCard.kt

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

// UnmatchedEventsSection has been extracted to UnmatchedEventsCard.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    title: String,
    initialDate: kotlinx.datetime.LocalDateTime?, // 🔧 ΠΡΟΣΘΗΚΗ: Προεπιλεγμένη ημερομηνία
    onDateSelected: (kotlinx.datetime.LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    // 🔧 FIX: Ρυθμίζουμε την αρχική ημερομηνία του picker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toEpochMillis() ?: Clock.System.now().toEpochMilliseconds()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                        // Κρατάμε την ώρα από το original date ή ρυθμίζουμε default (12:00)
                        val finalDateTime = if (initialDate != null) {
                            LocalDateTime(localDate.date, initialDate.time)
                        } else {
                            LocalDateTime(localDate.date, LocalTime(12, 0))
                        }
                        onDateSelected(finalDateTime)
                    }
                }
            ) {
                Text(Strings.Common.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.Common.cancel)
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
            },
            headline = {
                // 🔧 ΠΡΟΣΘΗΚΗ: Εμφανίζουμε το επιλεγμένο date σε Greek format
                Text(
                    text = datePickerState.selectedDateMillis?.let {
                        it.toLocalDate().toGreekDateString()
                    } ?: "Επιλέξτε ημερομηνία",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = PayrollColors.Primary
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredefinedPeriodDropdown(
    currentStartDate: LocalDateTime?,
    currentEndDate: LocalDateTime?,
    onPeriodSelected: (Pair<LocalDateTime, LocalDateTime>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val periods = remember {
        listOf(
            "2 Εργάσιμες Εβδομάδες" to DateRanges.twoWorkWeeks(),
            "Τρέχουσα Εβδομάδα" to DateRanges.thisWorkWeek(),
            "Προηγούμενη Εβδομάδα" to DateRanges.lastWorkWeek(),
            "Τελευταίες 2 Εβδομάδες" to DateRanges.lastTwoWorkWeeks(),
            "Τρέχων Μήνας" to DateRanges.thisMonth(),
            "Προηγούμενος Μήνας" to DateRanges.lastMonth(),
            "Τελευταίες 10 Εργάσιμες" to DateRanges.lastWorkDays(10),
            "Σήμερα" to DateRanges.today()
        )
    }

    // 🎯 Smart detection: Βρίσκουμε αν η τρέχουσα επιλογή ταιριάζει με κάποια προκαθορισμένη περίοδο
    val detectedPeriod = remember(currentStartDate, currentEndDate) {
        if (currentStartDate != null && currentEndDate != null) {
            periods.find { (_, range) ->
                // Συγκρίνουμε τις ημερομηνίες (αγνοούμε τις ώρες για την σύγκριση)
                range.first.date == currentStartDate.date &&
                        range.second.date == currentEndDate.date
            }?.first
        } else null
    }

    // Display text που δείχνει είτε το detected period είτε τις custom ημερομηνίες
    val displayText = when {
        detectedPeriod != null -> {
            detectedPeriod // Μόνο το όνομα της περιόδου για cleaner look
        }
        currentStartDate != null && currentEndDate != null -> {
            "Προσαρμοσμένη Περίοδος"
        }
        else -> ""
    }

    // Subtitle text που δείχνει τις ημερομηνίες
    val subtitleText = when {
        currentStartDate != null && currentEndDate != null -> {
            "${currentStartDate.toGreekDateString()} - ${currentEndDate.toGreekDateString()}"
        }
        else -> "Επιλέξτε περίοδο..."
    }

    Column(modifier = modifier) {
        Text(
            text = "Γρήγορη Επιλογή Περιόδου",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PayrollColors.OnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                value = displayText,
                onValueChange = {},
                placeholder = if (displayText.isEmpty()) {
                    {
                        Text(
                            subtitleText,
                            color = PayrollColors.TextSecondary
                        )
                    }
                } else null,
                supportingText = if (displayText.isNotEmpty()) {
                    {
                        Text(
                            text = subtitleText,
                            fontSize = 12.sp,
                            color = PayrollColors.TextSecondary
                        )
                    }
                } else null,
                leadingIcon = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = PayrollColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow",
                        tint = PayrollColors.Primary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PayrollColors.Primary,
                    unfocusedBorderColor = PayrollColors.DividerColor
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = if (displayText.isNotEmpty()) PayrollColors.OnSurface else PayrollColors.TextSecondary
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                periods.forEach { (name, dateRange) ->
                    val isSelected = detectedPeriod == name

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isSelected) PayrollColors.Primary else PayrollColors.OnSurface
                                    )
                                    Text(
                                        text = dateRange.toReadableString(),
                                        fontSize = 12.sp,
                                        color = PayrollColors.TextSecondary
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Επιλεγμένο",
                                        tint = PayrollColors.Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onPeriodSelected(dateRange)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (isSelected) PayrollColors.Primary else PayrollColors.OnSurface
                        )
                    )
                }
            }
        }

        // 💡 Smart helper text
        if (detectedPeriod != null) {
            Text(
                text = "✅ Προκαθορισμένη περίοδος: $detectedPeriod",
                fontSize = 12.sp,
                color = PayrollColors.Success,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else if (currentStartDate != null && currentEndDate != null) {
            Text(
                text = "🎯 Προσαρμοσμένη περίοδος - μπορείτε να επιλέξετε από τις παραπάνω",
                fontSize = 12.sp,
                color = PayrollColors.Info,
                modifier = Modifier.padding(top = 4.dp)
            )
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