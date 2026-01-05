package com.payroll.app.desktop.google

import eu.anifantakis.lib.ksafe.KSafe
import java.io.File

/**
 * Secure credential storage using KSafe encryption
 *
 * Stores sensitive Google OAuth credentials encrypted on disk
 * instead of hardcoding them in the codebase.
 */
class SecureCredentialStore {

    companion object {
        private const val STORE_NAME = "google-credentials"
        private const val KEY_CLIENT_ID = "client_id"
        private const val KEY_CLIENT_SECRET = "client_secret"
        private const val KEY_PROJECT_ID = "project_id"
    }

    private val kSafe: KSafe = KSafe(fileName = STORE_NAME)

    /**
     * Store Google OAuth credentials securely
     */
    fun storeCredentials(clientId: String, clientSecret: String, projectId: String) {
        kSafe.putDirect(KEY_CLIENT_ID, clientId)
        kSafe.putDirect(KEY_CLIENT_SECRET, clientSecret)
        kSafe.putDirect(KEY_PROJECT_ID, projectId)
        println("✅ Credentials stored securely")
    }

    /**
     * Retrieve client ID
     */
    fun getClientId(): String? = kSafe.getDirect(KEY_CLIENT_ID, null as String?)

    /**
     * Retrieve client secret
     */
    fun getClientSecret(): String? = kSafe.getDirect(KEY_CLIENT_SECRET, null as String?)

    /**
     * Retrieve project ID
     */
    fun getProjectId(): String? = kSafe.getDirect(KEY_PROJECT_ID, null as String?)

    /**
     * Check if credentials are stored
     */
    fun hasCredentials(): Boolean {
        return getClientId() != null && getClientSecret() != null
    }

    /**
     * Delete stored credentials
     */
    fun clearCredentials() {
        kSafe.putDirect(KEY_CLIENT_ID, "")
        kSafe.putDirect(KEY_CLIENT_SECRET, "")
        kSafe.putDirect(KEY_PROJECT_ID, "")
        println("🗑️ Credentials cleared")
    }

    /**
     * Import credentials from credentials.json file
     * This is a one-time setup operation
     */
    fun importFromJsonFile(credentialsFile: File): Boolean {
        return try {
            val jsonContent = credentialsFile.readText()

            // Parse JSON manually (simple extraction)
            val clientIdMatch = Regex(""""client_id":"([^"]+)"""").find(jsonContent)
            val clientSecretMatch = Regex(""""client_secret":"([^"]+)"""").find(jsonContent)
            val projectIdMatch = Regex(""""project_id":"([^"]+)"""").find(jsonContent)

            if (clientIdMatch != null && clientSecretMatch != null) {
                val clientId = clientIdMatch.groupValues[1]
                val clientSecret = clientSecretMatch.groupValues[1]
                val projectId = projectIdMatch?.groupValues?.get(1) ?: "unknown"

                storeCredentials(clientId, clientSecret, projectId)
                println("✅ Credentials imported from ${credentialsFile.absolutePath}")
                true
            } else {
                println("❌ Invalid credentials.json format")
                false
            }
        } catch (e: Exception) {
            println("❌ Failed to import credentials: ${e.message}")
            false
        }
    }
}