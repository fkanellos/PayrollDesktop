package com.payroll.app.desktop.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.File
import java.io.InputStreamReader

/**
 * Provides Google API credentials and services
 *
 * IMPORTANT: You need to place credentials.json in your resources/data folder
 * Get it from: https://console.cloud.google.com/
 */
class GoogleCredentialProvider {

    companion object {
        private const val APPLICATION_NAME = "Payroll Desktop App"
        private val JSON_FACTORY = GsonFactory.getDefaultInstance()

        private val SCOPES = listOf(
            CalendarScopes.CALENDAR_READONLY,
            SheetsScopes.SPREADSHEETS,
            DriveScopes.DRIVE_READONLY,
            "https://www.googleapis.com/auth/drive.file"
        )

        private val TOKENS_DIR = File(System.getProperty("user.home"), ".payroll-app/tokens")
    }

    private val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private var credential: Credential? = null

    init {
        try {
            credential = loadCredential()
        } catch (e: Exception) {
            println("Failed to load credentials: ${e.message}")
            println("Please ensure credentials.json exists in resources/data/")
        }
    }

    private fun loadCredential(): Credential {
        if (!TOKENS_DIR.exists()) {
            TOKENS_DIR.mkdirs()
        }

        // Load credentials.json from resources
        val credentialsStream = javaClass.getResourceAsStream("/data/credentials.json")
            ?: throw RuntimeException("credentials.json not found in resources/data/")

        val clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY,
            InputStreamReader(credentialsStream)
        )

        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(TOKENS_DIR))
            .setAccessType("offline")
            .build()

        // Try to load existing credential
        val existingCredential = flow.loadCredential("user")
        if (existingCredential != null && existingCredential.accessToken != null) {
            println("Loaded existing credentials")
            return existingCredential
        }

        // No credential found, need to authorize
        println("No credentials found. Browser will open for authorization...")
        val receiver = LocalServerReceiver.Builder()
            .setPort(8889)
            .build()

        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    /**
     * Get Google Calendar service
     */
    fun getCalendarService(): Calendar? {
        val cred = credential ?: return null
        return Calendar.Builder(httpTransport, JSON_FACTORY, cred)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    /**
     * Get Google Sheets service
     */
    fun getSheetsService(): Sheets? {
        val cred = credential ?: return null
        return Sheets.Builder(httpTransport, JSON_FACTORY, cred)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    /**
     * Check if authenticated
     */
    fun isAuthenticated(): Boolean = credential != null

    /**
     * Re-authenticate with Google
     */
    fun authenticate(): Boolean {
        return try {
            credential = loadCredential()
            true
        } catch (e: Exception) {
            println("Authentication failed: ${e.message}")
            false
        }
    }

    /**
     * Delete stored credentials and force re-authentication
     */
    fun deleteCredentials() {
        TOKENS_DIR.listFiles()?.forEach { it.delete() }
        credential = null
        println("Credentials deleted. Re-authorization required.")
    }

    /**
     * Get tokens directory path
     */
    fun getTokensPath(): String = TOKENS_DIR.absolutePath
}
