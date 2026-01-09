package com.payroll.app.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.payroll.app.desktop.core.di.commonModule
import com.payroll.app.desktop.core.di.useCaseModule
import com.payroll.app.desktop.core.di.viewModelModule
import com.payroll.app.desktop.di.localModule
import com.payroll.app.desktop.google.GoogleCredentialProvider
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.get
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import kotlin.system.exitProcess

fun main() {
    // Single instance lock - prevent multiple instances from running
    val lockFile = File(System.getProperty("user.home"), ".payroll-app/app.lock")

    // Ensure parent directory exists
    if (!lockFile.parentFile.exists() && !lockFile.parentFile.mkdirs()) {
        println("❌ ERROR: Cannot create lock directory at ${lockFile.parentFile.absolutePath}")
        println("   Permission denied or disk full. Please check file system permissions.")
        exitProcess(1)
    }

    // 🔥 FIX FILE HANDLE LEAK: Proper resource management with lateinit vars
    lateinit var randomAccessFile: RandomAccessFile
    lateinit var lock: FileLock

    try {
        randomAccessFile = RandomAccessFile(lockFile, "rw")

        try {
            val acquiredLock: FileLock? = randomAccessFile.channel.tryLock()

            if (acquiredLock == null) {
                // Another instance is running
                randomAccessFile.close()
                println("❌ Payroll Desktop is already running!")
                println("   Close the existing instance before starting a new one.")
                exitProcess(1)
            }

            lock = acquiredLock

            // Register shutdown hook to release lock
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    lock.release()
                    randomAccessFile.close()
                    lockFile.delete()
                } catch (e: Exception) {
                    System.err.println("⚠️ Warning: Failed to release lock: ${e.message}")
                }
            })

        } catch (lockException: Exception) {
            // Ensure file is closed if lock acquisition fails
            randomAccessFile.close()
            throw lockException
        }

    } catch (e: Exception) {
        println("❌ CRITICAL: Could not create lock file: ${e.message}")
        println("   Reason: ${e.javaClass.simpleName}")
        println("   This could allow multiple instances to run, which may corrupt data.")
        println("   Please check:")
        println("     1. File system permissions for ${lockFile.absolutePath}")
        println("     2. Available disk space")
        println("     3. Anti-virus software blocking file access")
        exitProcess(1)
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
            onCloseRequest = {
                // 🔥 FIX RESOURCE LEAK: Shutdown HTTP transport before exit
                try {
                    val credentialProvider = get<GoogleCredentialProvider>(GoogleCredentialProvider::class.java)
                    credentialProvider.shutdown()
                } catch (e: Exception) {
                    System.err.println("⚠️ Warning: Failed to shutdown Google transport: ${e.message}")
                }

                // Cleanup Koin
                stopKoin()

                exitApplication()
            },
            title = "Payroll Desktop",
        ) {
            App()
        }
    }
}