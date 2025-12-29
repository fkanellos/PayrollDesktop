package com.payroll.app.desktop.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * Factory for creating SQLite database driver for desktop
 */
object DriverFactory {

    private const val DATABASE_NAME = "payroll.db"
    private const val APP_FOLDER = ".payroll-app"

    /**
     * Create SQLite driver with database file in user's home directory
     */
    fun createDriver(): SqlDriver {
        // Store in user's home directory
        val dbPath = File(System.getProperty("user.home"), APP_FOLDER)
        if (!dbPath.exists()) {
            dbPath.mkdirs()
        }

        val dbFile = File(dbPath, DATABASE_NAME)
        val isNewDatabase = !dbFile.exists() || dbFile.length() == 0L

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        // Create tables if they don't exist
        if (isNewDatabase) {
            println("Creating new database at: ${dbFile.absolutePath}")
            PayrollDatabase.Schema.create(driver)
        }

        return driver
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
