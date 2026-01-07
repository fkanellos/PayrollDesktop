package com.payroll.app.desktop.google

import com.payroll.app.desktop.core.logging.Logger
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
        private const val TAG = "SecureCredentialStore"
        private const val STORE_NAME = "googlecredentials"
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
        Logger.info(TAG, "Credentials stored securely")
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
        Logger.info(TAG, "Credentials cleared")
    }

    /**
     * Import credentials from credentials.json file
     * This is a one-time setup operation
     */
    fun importFromJsonFile(credentialsFile: File): Boolean {
        return try {
            val jsonContent = credentialsFile.readText()
            Logger.info(TAG, "Parsing credentials.json...")

            // Parse JSON manually (simple extraction)
            val clientIdMatch = Regex(""""client_id"\s*:\s*"([^"]+)"""").find(jsonContent)
            val clientSecretMatch = Regex(""""client_secret"\s*:\s*"([^"]+)"""").find(jsonContent)
            val projectIdMatch = Regex(""""project_id"\s*:\s*"([^"]+)"""").find(jsonContent)

            Logger.debug(TAG, "clientIdMatch: ${clientIdMatch != null}")
            Logger.debug(TAG, "clientSecretMatch: ${clientSecretMatch != null}")
            Logger.debug(TAG, "projectIdMatch: ${projectIdMatch != null}")

            if (clientIdMatch != null && clientSecretMatch != null) {
                val clientId = clientIdMatch.groupValues[1]
                val clientSecret = clientSecretMatch.groupValues[1]
                val projectId = projectIdMatch?.groupValues?.get(1) ?: "unknown"

                Logger.info(TAG, "Storing credentials...")
                storeCredentials(clientId, clientSecret, projectId)
                Logger.info(TAG, "Credentials imported from ${credentialsFile.absolutePath}")
                true
            } else {
                Logger.error(TAG, "Invalid credentials.json format")
                Logger.debug(TAG, "First 200 chars: ${jsonContent.take(200)}")
                false
            }
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to import credentials", e)
            false
        }
    }
}