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
import com.payroll.app.desktop.core.config.AppConfig
import com.payroll.app.desktop.core.logging.Logger
import java.io.File
import java.io.InputStreamReader

/**
 * Provides Google API credentials and services
 *
 * SECURITY: Uses KSafe encrypted storage for OAuth credentials
 *
 * Setup Steps:
 * 1. Get credentials.json from Google Cloud Console
 *    - Go to: https://console.cloud.google.com/
 *    - APIs & Services → Credentials → Create OAuth Client ID
 *
 * 2. Import credentials on first run:
 *    - The app will prompt for credentials.json location
 *    - OR manually place it in ~/.payroll-app/ and run import
 *
 * 3. Set OAuth Consent Screen to PRODUCTION (not Testing)
 *    - Testing mode: tokens expire in 7 days
 *    - Production mode: tokens last indefinitely (until revoked or 6 months unused)
 *
 * 4. Tokens are stored in: ~/.payroll-app/tokens/
 *    - Access tokens auto-refresh every ~1 hour
 *    - Refresh tokens last 6 months (if used within that period)
 */
class GoogleCredentialProvider(
    private val appConfig: AppConfig
) {

    companion object {
        private const val TAG = "GoogleCredentialProvider"
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
    private val secureStore = SecureCredentialStore()
    private var credential: Credential? = null

    /**
     * 🔥 HIGH FIX: Fail-fast initialization with clear error messages
     * Old behavior: Silent null credential → mysterious NPEs later
     * New behavior: Fail immediately with actionable error message
     */
    init {
        try {
            credential = loadCredential()
            Logger.info(TAG, "✅ Google credentials loaded successfully")
        } catch (e: RuntimeException) {
            // Credential errors should fail-fast with clear messages
            Logger.error(TAG, "❌ CRITICAL: Failed to load Google credentials", e)
            Logger.error(TAG, "   Error: ${e.message}")
            Logger.error(TAG, "")
            Logger.error(TAG, "   🔧 Setup Instructions:")
            Logger.error(TAG, "   1. Get credentials.json from Google Cloud Console")
            Logger.error(TAG, "      → https://console.cloud.google.com/")
            Logger.error(TAG, "   2. Place it in: ~/.payroll-app/credentials.json")
            Logger.error(TAG, "   3. Run the app to import it")
            Logger.error(TAG, "")
            throw IllegalStateException(
                "Google API credentials not configured. " +
                "See console output for setup instructions.", e
            )
        } catch (e: Exception) {
            // Unexpected errors should also fail-fast
            Logger.error(TAG, "❌ CRITICAL: Unexpected error during credential initialization", e)
            throw IllegalStateException(
                "Failed to initialize Google credentials: ${e.message}", e
            )
        }
    }

    private fun loadCredential(): Credential {
        if (!TOKENS_DIR.exists()) {
            TOKENS_DIR.mkdirs()
        }

        // Check if credentials exist in secure storage
        if (!secureStore.hasCredentials()) {
            throw RuntimeException(
                "Google OAuth credentials not found!\n" +
                "Please run setup to import credentials.json\n" +
                "See README.md for setup instructions"
            )
        }

        // Load credentials from secure storage
        val clientId = secureStore.getClientId() ?: throw RuntimeException("Client ID not found")
        val clientSecret = secureStore.getClientSecret() ?: throw RuntimeException("Client secret not found")

        // Build GoogleClientSecrets from stored credentials
        val clientSecrets = GoogleClientSecrets().apply {
            installed = GoogleClientSecrets.Details().apply {
                this.clientId = clientId
                this.clientSecret = clientSecret
                this.authUri = "https://accounts.google.com/o/oauth2/auth"
                this.tokenUri = "https://oauth2.googleapis.com/token"
                this.redirectUris = listOf(appConfig.oauthRedirectUri)
            }
        }

        // 🔒 SECURITY: Ensure tokens directory has secure permissions
        ensureSecureTokensDirectory()

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
            Logger.info(TAG, "Loaded existing credentials")
            return existingCredential
        }

        // No credential found, need to authorize
        Logger.info(TAG, "No credentials found. Browser will open for authorization...")
        val receiver = LocalServerReceiver.Builder()
            .setPort(8889)
            .build()

        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    /**
     * Get Google Calendar service
     *
     * 🔥 HIGH FIX: Fail-fast validation instead of returning null
     * Now throws IllegalStateException with clear error if credentials missing
     */
    fun getCalendarService(): Calendar {
        val cred = credential
            ?: throw IllegalStateException(
                "Cannot create Calendar service: Google credentials not initialized. " +
                "Please run setup to import credentials."
            )

        // Auto-refresh if token is expired or about to expire
        try {
            if (cred.expiresInSeconds != null && cred.expiresInSeconds <= 300) {
                Logger.info(TAG, "Access token expiring soon, refreshing...")
                cred.refreshToken()
                Logger.info(TAG, "Token refreshed successfully")
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Token refresh failed: ${e.message}")
        }

        return Calendar.Builder(httpTransport, JSON_FACTORY, cred)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    /**
     * Get Google Sheets service
     *
     * 🔥 HIGH FIX: Fail-fast validation instead of returning null
     * Now throws IllegalStateException with clear error if credentials missing
     */
    fun getSheetsService(): Sheets {
        val cred = credential
            ?: throw IllegalStateException(
                "Cannot create Sheets service: Google credentials not initialized. " +
                "Please run setup to import credentials."
            )

        // Auto-refresh if token is expired or about to expire
        try {
            if (cred.expiresInSeconds != null && cred.expiresInSeconds <= 300) {
                Logger.info(TAG, "Access token expiring soon, refreshing...")
                cred.refreshToken()
                Logger.info(TAG, "Token refreshed successfully")
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Token refresh failed: ${e.message}")
        }

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
            Logger.error(TAG, "Authentication failed: ${e.message}")
            false
        }
    }

    /**
     * Delete stored credentials and force re-authentication
     */
    fun deleteCredentials() {
        TOKENS_DIR.listFiles()?.forEach { it.delete() }
        credential = null
        Logger.info(TAG, "Credentials deleted. Re-authorization required.")
    }

    /**
     * Get tokens directory path
     */
    fun getTokensPath(): String = TOKENS_DIR.absolutePath

    /**
     * Import credentials from credentials.json file
     * This is a one-time setup operation
     */
    fun importCredentials(credentialsFile: File): Boolean {
        return secureStore.importFromJsonFile(credentialsFile)
    }

    /**
     * Check if credentials are set up
     */
    fun hasCredentials(): Boolean = secureStore.hasCredentials()

    /**
     * 🔒 SECURITY: Ensure tokens directory exists with secure permissions
     * Prevents other users on the system from reading OAuth tokens
     */
    private fun ensureSecureTokensDirectory() {
        if (!TOKENS_DIR.exists()) {
            TOKENS_DIR.mkdirs()
        }

        try {
            // Remove all permissions for group and others
            TOKENS_DIR.setReadable(false, false)
            TOKENS_DIR.setWritable(false, false)
            TOKENS_DIR.setExecutable(false, false)

            // Grant all permissions to owner only (0700)
            TOKENS_DIR.setReadable(true, true)
            TOKENS_DIR.setWritable(true, true)
            TOKENS_DIR.setExecutable(true, true)

            Logger.info(TAG, "🔒 Set secure permissions (0700) on tokens directory")

            // Also secure any existing token files
            TOKENS_DIR.listFiles()?.forEach { file ->
                file.setReadable(false, false)
                file.setWritable(false, false)
                file.setExecutable(false, false)
                file.setReadable(true, true)
                file.setWritable(true, true)
            }
        } catch (e: Exception) {
            Logger.error(TAG, "⚠️ Could not set directory permissions: ${e.message}")
        }
    }

    /**
     * 🔥 FIX RESOURCE LEAK: Shutdown HTTP transport to release resources
     * Should be called on app shutdown
     */
    fun shutdown() {
        try {
            httpTransport.shutdown()
            Logger.info(TAG, "HTTP transport shutdown successfully")
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to shutdown HTTP transport: ${e.message}")
        }
    }
}
