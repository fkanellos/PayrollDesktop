package com.payroll.app.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.payroll.app.desktop.core.di.commonModule
import org.koin.core.context.startKoin


fun main() = application {
    startKoin {
        modules(commonModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Payrolldesktop",
    ) {
        App()
    }
}