package com.payroll.app.desktop.ui.components.dropdowns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payroll.app.desktop.domain.models.Employee
import com.payroll.app.desktop.ui.theme.PayrollColors
import com.payroll.app.desktop.ui.theme.PayrollTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PayrollDropdown(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Επιλέξτε...",
    enabled: Boolean = true,
    isError: Boolean = false,
    displayText: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isError) PayrollColors.Error else PayrollColors.OnSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Dropdown
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
                value = selectedItem?.let(displayText) ?: "",
                onValueChange = {},
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) PayrollColors.Error else PayrollColors.Surface,
                    unfocusedBorderColor = if (isError) PayrollColors.Error else PayrollColors.DividerColor,
                    disabledBorderColor = PayrollColors.DividerColor.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = enabled,
                isError = isError
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = CardDefaults.cardColors().contentColor,
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = displayText(item),
                                style = TextStyle(fontSize = 14.sp)
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = PayrollColors.OnSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeDropdown(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    onEmployeeSelected: (Employee) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Εργαζόμενος",
    enabled: Boolean = true,
    isError: Boolean = false
) {
    PayrollDropdown(
        items = employees,
        selectedItem = selectedEmployee,
        onItemSelected = onEmployeeSelected,
        modifier = modifier,
        label = label,
        placeholder = "Επιλέξτε εργαζόμενο...",
        enabled = enabled,
        isError = isError,
        displayText = { it.name }
    )
}

@Composable
fun PeriodDropdown(
    selectedPeriod: String?,
    onPeriodSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Περίοδος",
    enabled: Boolean = true
) {
    val periods = listOf(
        "Τρέχουσα δίβδομη περίοδος",
        "Προηγούμενη δίβδομη περίοδος",
        "Τρέχων μήνας",
        "Προηγούμενος μήνας",
        "Προσαρμοσμένη περίοδος"
    )

    PayrollDropdown(
        items = periods,
        selectedItem = selectedPeriod,
        onItemSelected = onPeriodSelected,
        modifier = modifier,
        label = label,
        placeholder = "Επιλέξτε περίοδο...",
        enabled = enabled
    )
}

// Preview Composables
@Preview
@Composable
private fun PayrollDropdownPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val items = listOf("Option 1", "Option 2", "Option 3")
                var selectedItem by remember { mutableStateOf<String?>(null) }

                PayrollDropdown(
                    items = items,
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    label = "Επιλογές",
                    placeholder = "Επιλέξτε μια επιλογή..."
                )

                // Error state
                PayrollDropdown(
                    items = items,
                    selectedItem = null,
                    onItemSelected = { },
                    label = "Με Σφάλμα",
                    placeholder = "Αυτό έχει σφάλμα...",
                    isError = true
                )

                // Disabled state
                PayrollDropdown(
                    items = items,
                    selectedItem = items.first(),
                    onItemSelected = { },
                    label = "Απενεργοποιημένο",
                    enabled = false
                )
            }
        }
    }
}

@Preview
@Composable
private fun EmployeeDropdownPreview() {
    PayrollTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val employees = listOf(
                    Employee(
                        id = "1",
                        name = "Αγγελική Γκουντοπούλου",
                        email = "psy.gkountopoulou@gmail.com",
                        calendarId = "calendar1"
                    ),
                    Employee(
                        id = "2",
                        name = "Γιάννης Παπαδόπουλος",
                        email = "giannis@example.com",
                        calendarId = "calendar2"
                    )
                )

                var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

                EmployeeDropdown(
                    employees = employees,
                    selectedEmployee = selectedEmployee,
                    onEmployeeSelected = { selectedEmployee = it }
                )

                var selectedPeriod by remember { mutableStateOf<String?>(null) }

                PeriodDropdown(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }
        }
    }
}