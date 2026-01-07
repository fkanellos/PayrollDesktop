package com.payroll.app.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.payroll.app.desktop.core.di.commonModule
import com.payroll.app.desktop.core.di.useCaseModule
import com.payroll.app.desktop.core.di.viewModelModule
import com.payroll.app.desktop.di.localModule
import com.payroll.app.desktop.google.GoogleCredentialProvider
import org.koin.core.context.startKoin
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import kotlin.system.exitProcess

fun main() {
    // Single instance lock - prevent multiple instances from running
    val lockFile = File(System.getProperty("user.home"), ".payroll-app/app.lock")
    lockFile.parentFile?.mkdirs()

    try {
        val randomAccessFile = RandomAccessFile(lockFile, "rw")
        val lock: FileLock? = randomAccessFile.channel.tryLock()

        if (lock == null) {
            println("❌ Payroll Desktop is already running!")
            println("   Close the existing instance before starting a new one.")
            exitProcess(1)
        }

        // Register shutdown hook to release lock
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                lock.release()
                randomAccessFile.close()
                lockFile.delete()
            } catch (e: Exception) {
                // Ignore
            }
        })

    } catch (e: Exception) {
        println("⚠️ Could not create lock file: ${e.message}")
        // Continue anyway
    }

    application {
        // Initialize Koin DI with all modules
        startKoin {
            modules(
                commonModule,      // Common dependencies
                localModule,       // Local database and Google Calendar
                useCaseModule,     // Domain UseCases
                viewModelModule    // ViewModels
            )
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Payroll Desktop",
        ) {
            App()
        }
    }
}