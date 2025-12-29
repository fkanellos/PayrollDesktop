package com.payroll.app.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.payroll.app.desktop.core.di.commonModule
import com.payroll.app.desktop.di.localModule
import org.koin.core.context.startKoin

fun main() = application {
    // Initialize Koin DI with both common and local modules
    startKoin {
        modules(
            commonModule,  // API-based repositories
            localModule    // Local database and Google Calendar
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Payroll Desktop",
    ) {
        App()
    }
}