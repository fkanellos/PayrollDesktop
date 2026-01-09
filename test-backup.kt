import com.payroll.app.desktop.data.services.DatabaseBackupService

fun main() {
    println("🧪 Testing DatabaseBackupService...")

    val backupService = DatabaseBackupService()

    // Test 1: Create manual backup
    println("\n📦 Test 1: Creating manual backup...")
    val backupFile = backupService.createBackup("test-manual")
    if (backupFile != null) {
        println("✅ Backup created: ${backupFile.name}")
        println("   Size: ${backupFile.length() / 1024} KB")
        println("   Path: ${backupFile.absolutePath}")
    } else {
        println("❌ Failed to create backup")
    }

    // Test 2: Get backup history
    println("\n📜 Test 2: Getting backup history...")
    val backups = backupService.getBackupHistory()
    println("Found ${backups.size} backups:")
    backups.forEach { backup ->
        println("  - ${backup.name} (${backup.sizeKB} KB)")
    }

    // Test 3: Get backups directory
    println("\n📁 Test 3: Backups directory...")
    println("Path: ${backupService.getBackupsPath()}")
    println("Total size: ${backupService.getTotalBackupSize()} KB")

    println("\n✅ All tests completed!")
}
