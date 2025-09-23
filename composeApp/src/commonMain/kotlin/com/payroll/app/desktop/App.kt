package com.payroll.app.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.payroll.app.desktop.ui.screens.PayrollScreen
import com.payroll.app.desktop.ui.theme.PayrollTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    PayrollTheme {
        PayrollScreen()
    }
}