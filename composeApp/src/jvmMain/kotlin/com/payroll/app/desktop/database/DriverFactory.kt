package com.payroll.app.desktop.database

import com.payroll.app.desktop.core.logging.Logger

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * Factory for creating SQLite database driver for desktop
 *
 * 🔒 Security Features:
 * - Secure directory permissions (0700 - owner-only access)
 * - Database stored in protected user directory
 * - OS-level encryption recommended (BitLocker/FileVault)
 *
 * Note: For full database encryption, use OS-level tools:
 * - macOS: FileVault 2
 * - Windows: BitLocker
 * - Linux: LUKS/dm-crypt
 */
object DriverFactory {

    private const val TAG = "DriverFactory"
    private const val DATABASE_NAME = "payroll.db"
    private const val APP_FOLDER = ".payroll-app"

    /**
     * Create SQLite driver with database file in user's home directory
     *
     * Security measures:
     * 1. Directory created with 0700 permissions (owner-only)
     * 2. Files inaccessible to other users on the system
     * 3. Recommend enabling OS-level disk encryption
     */
    fun createDriver(): SqlDriver {
        // Store in user's home directory
        val dbPath = File(System.getProperty("user.home"), APP_FOLDER)
        if (!dbPath.exists()) {
            dbPath.mkdirs()
            // 🔒 SECURITY: Set directory permissions to 0700 (owner-only access)
            setSecureDirectoryPermissions(dbPath)
        } else {
            // Ensure permissions are correct even if directory exists
            setSecureDirectoryPermissions(dbPath)
        }

        val dbFile = File(dbPath, DATABASE_NAME)
        val isNewDatabase = !dbFile.exists() || dbFile.length() == 0L

        // 🔒 Set secure permissions on database file
        if (dbFile.exists()) {
            setSecureFilePermissions(dbFile)
        }

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        // Create tables if they don't exist
        if (isNewDatabase) {
            Logger.info(TAG, "Creating new database at: ${dbFile.absolutePath}")
            PayrollDatabase.Schema.create(driver)
            // Set permissions on newly created file
            setSecureFilePermissions(dbFile)
        }

        Logger.info(TAG, "✅ Database opened successfully with secure permissions")
        return driver
    }

    /**
     * Set secure directory permissions (0700 - owner-only access)
     *
     * This prevents other users on the system from reading:
     * - The database file
     * - Token files
     * - Configuration files
     *
     * Unix permissions: rwx------ (700)
     */
    private fun setSecureDirectoryPermissions(directory: File) {
        try {
            // Remove all permissions for group and others
            directory.setReadable(false, false)
            directory.setWritable(false, false)
            directory.setExecutable(false, false)

            // Grant all permissions to owner only
            directory.setReadable(true, true)   // Owner can read
            directory.setWritable(true, true)   // Owner can write
            directory.setExecutable(true, true) // Owner can enter directory

            Logger.info(TAG, "🔒 Set directory permissions (0700) on: ${directory.absolutePath}")
        } catch (e: Exception) {
            Logger.error(TAG, "⚠️ Could not set directory permissions: ${e.message}")
        }
    }

    /**
     * Set secure file permissions (0600 - owner read/write only)
     *
     * Unix permissions: rw------- (600)
     */
    private fun setSecureFilePermissions(file: File) {
        try {
            // Remove all permissions for group and others
            file.setReadable(false, false)
            file.setWritable(false, false)
            file.setExecutable(false, false)

            // Grant read/write to owner only
            file.setReadable(true, true)   // Owner can read
            file.setWritable(true, true)   // Owner can write

            Logger.debug(TAG, "🔒 Set file permissions (0600) on: ${file.name}")
        } catch (e: Exception) {
            Logger.error(TAG, "⚠️ Could not set file permissions: ${e.message}")
        }
    }

    /**
     * Get the database file path
     */
    fun getDatabasePath(): String {
        val dbPath = File(System.getProperty("user.home"), APP_FOLDER)
        return File(dbPath, DATABASE_NAME).absolutePath
    }

    /**
     * Delete the database file (for testing/reset)
     */
    fun deleteDatabase(): Boolean {
        val dbPath = File(System.getProperty("user.home"), APP_FOLDER)
        val dbFile = File(dbPath, DATABASE_NAME)
        return if (dbFile.exists()) {
            dbFile.delete()
        } else {
            true
        }
    }
}
