package com.payroll.app.desktop.data.services

import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.database.DriverFactory
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Service για auto-backup και manual backup/restore του database
 *
 * Features:
 * - Auto-backup on app shutdown
 * - Manual backup with timestamp
 * - Manual restore from backup file
 * - Backup history management (keep last N backups)
 * - Compressed backups (ZIP format)
 *
 * Backup location: ~/.payroll-app/backups/
 * Backup format: payroll_backup_YYYY-MM-DD_HH-MM-SS.zip
 */
class DatabaseBackupService {

    companion object {
        private const val TAG = "DatabaseBackupService"
        private const val APP_FOLDER = ".payroll-app"
        private const val BACKUPS_FOLDER = "backups"
        private const val MAX_BACKUPS = 10 // Keep last 10 backups
        private const val DATABASE_NAME = "payroll.db"
    }

    private val backupsDir: File by lazy {
        val appDir = File(System.getProperty("user.home"), APP_FOLDER)
        File(appDir, BACKUPS_FOLDER).apply {
            if (!exists()) {
                mkdirs()
                setSecureDirectoryPermissions(this)
            }
        }
    }

    /**
     * Create a backup of the current database
     *
     * @param reason Optional reason for the backup (e.g., "auto", "manual", "before-update")
     * @return Backup file or null if backup failed
     */
    fun createBackup(reason: String = "manual"): File? {
        return try {
            val dbPath = DriverFactory.getDatabasePath()
            val dbFile = File(dbPath)

            if (!dbFile.exists() || dbFile.length() == 0L) {
                Logger.warning(TAG, "❌ Database file does not exist or is empty")
                return null
            }

            // Generate backup filename with timestamp
            val timestamp = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .let { "${it.year}-${it.monthNumber.toString().padStart(2, '0')}-${it.dayOfMonth.toString().padStart(2, '0')}_${it.hour.toString().padStart(2, '0')}-${it.minute.toString().padStart(2, '0')}-${it.second.toString().padStart(2, '0')}" }

            val backupFileName = "payroll_backup_${reason}_${timestamp}.zip"
            val backupFile = File(backupsDir, backupFileName)

            // Create compressed backup
            ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
                zos.putNextEntry(ZipEntry(DATABASE_NAME))
                FileInputStream(dbFile).use { fis ->
                    fis.copyTo(zos)
                }
                zos.closeEntry()
            }

            // Set secure permissions on backup file
            setSecureFilePermissions(backupFile)

            Logger.info(TAG, "✅ Database backup created: ${backupFile.name} (${backupFile.length() / 1024} KB)")

            // Cleanup old backups
            cleanupOldBackups()

            backupFile
        } catch (e: Exception) {
            Logger.error(TAG, "❌ Failed to create backup", e)
            null
        }
    }

    /**
     * Restore database from a backup file
     *
     * @param backupFile The backup file to restore from
     * @return True if restore was successful
     */
    fun restoreFromBackup(backupFile: File): Boolean {
        return try {
            if (!backupFile.exists()) {
                Logger.error(TAG, "❌ Backup file does not exist: ${backupFile.name}")
                return false
            }

            val dbPath = DriverFactory.getDatabasePath()
            val dbFile = File(dbPath)

            // Create backup of current database before restore
            createBackup("before-restore")

            // Extract and restore from ZIP
            java.util.zip.ZipInputStream(FileInputStream(backupFile)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == DATABASE_NAME) {
                        FileOutputStream(dbFile).use { fos ->
                            zis.copyTo(fos)
                        }
                        break
                    }
                    entry = zis.nextEntry
                }
            }

            // Set secure permissions on restored database
            setSecureFilePermissions(dbFile)

            Logger.info(TAG, "✅ Database restored from: ${backupFile.name}")
            true
        } catch (e: Exception) {
            Logger.error(TAG, "❌ Failed to restore backup", e)
            false
        }
    }

    /**
     * Get list of all backup files, sorted by date (newest first)
     */
    fun getBackupHistory(): List<BackupInfo> {
        return try {
            backupsDir.listFiles { file -> file.extension == "zip" }
                ?.sortedByDescending { it.lastModified() }
                ?.map { file ->
                    BackupInfo(
                        file = file,
                        name = file.name,
                        sizeKB = file.length() / 1024,
                        timestamp = file.lastModified()
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            Logger.error(TAG, "❌ Failed to get backup history", e)
            emptyList()
        }
    }

    /**
     * Delete a specific backup file
     */
    fun deleteBackup(backupFile: File): Boolean {
        return try {
            if (backupFile.exists() && backupFile.delete()) {
                Logger.info(TAG, "✅ Backup deleted: ${backupFile.name}")
                true
            } else {
                Logger.warning(TAG, "❌ Failed to delete backup: ${backupFile.name}")
                false
            }
        } catch (e: Exception) {
            Logger.error(TAG, "❌ Error deleting backup", e)
            false
        }
    }

    /**
     * Auto-backup on app shutdown
     * Should be called from main() or app shutdown hook
     */
    fun autoBackupOnShutdown() {
        Logger.info(TAG, "🔄 Creating auto-backup on shutdown...")
        createBackup("auto-shutdown")
    }

    /**
     * Keep only the last N backups, delete older ones
     */
    private fun cleanupOldBackups() {
        try {
            val backups = backupsDir.listFiles { file -> file.extension == "zip" }
                ?.sortedByDescending { it.lastModified() }
                ?: return

            if (backups.size > MAX_BACKUPS) {
                val backupsToDelete = backups.drop(MAX_BACKUPS)
                backupsToDelete.forEach { backup ->
                    if (backup.delete()) {
                        Logger.info(TAG, "🗑️ Deleted old backup: ${backup.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error(TAG, "⚠️ Failed to cleanup old backups", e)
        }
    }

    /**
     * Set secure directory permissions (0700)
     */
    private fun setSecureDirectoryPermissions(directory: File) {
        try {
            directory.setReadable(false, false)
            directory.setWritable(false, false)
            directory.setExecutable(false, false)
            directory.setReadable(true, true)
            directory.setWritable(true, true)
            directory.setExecutable(true, true)
            Logger.debug(TAG, "🔒 Set directory permissions (0700) on: ${directory.name}")
        } catch (e: Exception) {
            Logger.error(TAG, "⚠️ Could not set directory permissions: ${e.message}")
        }
    }

    /**
     * Set secure file permissions (0600)
     */
    private fun setSecureFilePermissions(file: File) {
        try {
            file.setReadable(false, false)
            file.setWritable(false, false)
            file.setExecutable(false, false)
            file.setReadable(true, true)
            file.setWritable(true, true)
            Logger.debug(TAG, "🔒 Set file permissions (0600) on: ${file.name}")
        } catch (e: Exception) {
            Logger.error(TAG, "⚠️ Could not set file permissions: ${e.message}")
        }
    }

    /**
     * Get total size of all backups in KB
     */
    fun getTotalBackupSize(): Long {
        return try {
            backupsDir.listFiles { file -> file.extension == "zip" }
                ?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        } / 1024
    }

    /**
     * Get backups directory path
     */
    fun getBackupsPath(): String = backupsDir.absolutePath
}

/**
 * Backup file information
 */
data class BackupInfo(
    val file: File,
    val name: String,
    val sizeKB: Long,
    val timestamp: Long
)
