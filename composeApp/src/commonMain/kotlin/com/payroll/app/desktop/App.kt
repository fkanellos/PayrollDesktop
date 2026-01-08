package com.payroll.app.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.payroll.app.desktop.ui.screens.ClientManagementScreen
import com.payroll.app.desktop.ui.screens.EmployeeManagementScreen
import com.payroll.app.desktop.ui.screens.PayrollScreen
import com.payroll.app.desktop.ui.screens.SettingsScreen
import com.payroll.app.desktop.ui.theme.PayrollTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    PayrollTheme {
        MainNavigation()
    }
}

@Composable
fun MainNavigation() {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = listOf(
        "💰 Μισθοδοσία" to 0,
        "👥 Διαχείριση Πελατών" to 1,
        "👤 Διαχείριση Εργαζομένων" to 2,
        "⚙️ Ρυθμίσεις" to 3
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Tab Bar
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEach { (title, index) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }

        // Screen Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> PayrollScreen()
                1 -> ClientManagementScreen()
                2 -> EmployeeManagementScreen()
                3 -> SettingsScreen(onNavigateBack = { selectedTabIndex = 0 })
            }
        }
    }
}

// 🎨 PREVIEW FUNCTIONS
@Preview
@Composable
fun MainNavigationPreview() {
    PayrollTheme {
        MainNavigation()
    }
}

@Preview
@Composable
fun PayrollTabPreview() {
    PayrollTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = 0,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = true,
                    onClick = {},
                    text = { Text("💰 Μισθοδοσία") }
                )
                Tab(
                    selected = false,
                    onClick = {},
                    text = { Text("👥 Διαχείριση Πελατών") }
                )
            }
        }
    }
}

@Preview
@Composable
fun ClientsTabPreview() {
    PayrollTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = false,
                    onClick = {},
                    text = { Text("💰 Μισθοδοσία") }
                )
                Tab(
                    selected = true,
                    onClick = {},
                    text = { Text("👥 Διαχείριση Πελατών") }
                )
            }
            ClientManagementScreen()
        }
    }
}