package com.payroll.app.desktop

import com.payroll.app.desktop.google.GoogleCredentialProvider
import java.io.File
import kotlin.system.exitProcess

/**
 * Setup script to import Google OAuth credentials
 * Usage: Run this as a separate main function
 */
fun main() {
    println("🔐 Payroll Desktop - Credentials Setup")
    println("=" * 50)
    println()

    val credentialsFile = File("credentials.json")

    if (!credentialsFile.exists()) {
        println("❌ credentials.json not found in current directory")
        println()
        println("Please:")
        println("1. Download credentials.json from Google Cloud Console")
        println("2. Place it in: ${File(".").absolutePath}")
        println("3. Run this setup again")
        exitProcess(1)
    }

    println("✅ Found credentials.json")
    println()

    try {
        val credentialProvider = GoogleCredentialProvider()
        val success = credentialProvider.importCredentials(credentialsFile)

        if (success) {
            println()
            println("⏳ Waiting for DataStore to flush...")
            Thread.sleep(3000) // Wait for DataStore to persist
            println("✅ Setup Complete!")
            println("📁 Credentials encrypted and stored in: ~/.payroll-app/credentials/")
            println()
            println("Next step: Run the application")
            println("  ./gradlew :composeApp:run")
            println()
            exitProcess(0)
        } else {
            println("❌ Failed to import credentials")
            println("   Please check the file format")
            exitProcess(1)
        }
    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}

private operator fun String.times(count: Int): String = repeat(count)