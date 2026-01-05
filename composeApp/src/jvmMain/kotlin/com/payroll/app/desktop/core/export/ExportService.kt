package com.payroll.app.desktop.core.export

import com.payroll.app.desktop.domain.models.PayrollResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File

actual class ExportService actual constructor() {

    actual fun exportToPdf(payrollResult: PayrollResponse): ExportResult {
        return try {
            val timestamp = Clock.System.now().epochSeconds
            val employeeName = sanitizeFilename(payrollResult.employee.name)
            val filename = "Payroll_${employeeName}_${timestamp}.txt"

            val content = generatePdfContent(payrollResult)
            val filePath = saveToFile(filename, content)

            ExportResult.Success(filePath, "Text File")

        } catch (e: Exception) {
            ExportResult.Error("Σφάλμα δημιουργίας PDF: ${e.message}")
        }
    }

    actual fun exportToExcel(payrollResult: PayrollResponse): ExportResult {
        return try {
            val timestamp = Clock.System.now().epochSeconds
            val employeeName = sanitizeFilename(payrollResult.employee.name)
            val filename = "Payroll_${employeeName}_${timestamp}.csv"

            val content = generateCsvContent(payrollResult)
            val filePath = saveToFile(filename, content)

            ExportResult.Success(filePath, "CSV File")

        } catch (e: Exception) {
            ExportResult.Error("Σφάλμα δημιουργίας Excel: ${e.message}")
        }
    }

    // ADD THIS NEW METHOD
    actual fun savePdfBytes(pdfBytes: ByteArray, filename: String): ExportResult {
        return try {
            val filePath = saveBytesToFile(filename, pdfBytes)
            ExportResult.Success(filePath, "PDF File")
        } catch (e: Exception) {
            ExportResult.Error("Σφάλμα αποθήκευσης PDF: ${e.message}")
        }
    }

    private fun saveBytesToFile(filename: String, bytes: ByteArray): String {
        val userHome = System.getProperty("user.home") ?: "."
        val possiblePaths = listOf(
            "$userHome/Downloads",
            "$userHome/Desktop",
            "$userHome/Documents",
            "."
        )

        for (path in possiblePaths) {
            try {
                val dir = File(path)
                if (dir.exists() && dir.canWrite()) {
                    val file = File(dir, filename)
                    file.writeBytes(bytes)
                    return file.absolutePath
                }
            } catch (_: Exception) {
                continue
            }
        }

        val fallbackDir = File("$userHome/PayrollExports")
        fallbackDir.mkdirs()
        val file = File(fallbackDir, filename)
        file.writeBytes(bytes)
        return file.absolutePath
    }

    // Helpers (όπως είχες ήδη)
    private fun saveToFile(filename: String, content: String): String {
        val userHome = System.getProperty("user.home") ?: "."
        val possiblePaths = listOf(
            "$userHome/Downloads",
            "$userHome/Desktop",
            "$userHome/Documents",
            "."
        )

        for (path in possiblePaths) {
            try {
                val dir = File(path)
                if (dir.exists() && dir.canWrite()) {
                    val file = File(dir, filename)
                    file.writeText(content, Charsets.UTF_8)
                    return file.absolutePath
                }
            } catch (_: Exception) {
                continue
            }
        }

        val fallbackDir = File("$userHome/PayrollExports")
        fallbackDir.mkdirs()
        val file = File(fallbackDir, filename)
        file.writeText(content, Charsets.UTF_8)
        return file.absolutePath
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(" ", "_")
            .replace(Regex("[^a-zA-Z0-9_]"), "")
            .take(30)
    }

    private fun generatePdfContent(payroll: PayrollResponse): String {
        return buildString {
            appendLine("=" * 80)
            appendLine("                      ΑΝΑΦΟΡΑ ΜΙΣΘΟΔΟΣΙΑΣ")
            appendLine("=" * 80)
            appendLine()

            appendLine("ΣΤΟΙΧΕΙΑ ΕΡΓΑΖΟΜΕΝΟΥ:")
            appendLine("Όνομα: ${payroll.employee.name}")
            appendLine("Email: ${payroll.employee.email}")
            appendLine("Περίοδος: ${payroll.period}")
            appendLine("Δημιουργήθηκε: ${payroll.generatedAt}")
            appendLine()

            appendLine("-" * 50)
            appendLine("ΣΥΓΚΕΝΤΡΩΤΙΚΑ ΣΤΟΙΧΕΙΑ:")
            appendLine("-" * 50)
            appendLine("Συνολικές Συνεδρίες: ${payroll.summary.totalSessions}")
            appendLine("Συνολικά Έσοδα: €${payroll.summary.totalRevenue}")
            appendLine("Μισθός Εργαζομένου: €${payroll.summary.employeeEarnings}")
            appendLine("Κέρδη Εταιρίας: €${payroll.summary.companyEarnings}")
            appendLine()

            appendLine("-" * 50)
            appendLine("ΑΝΑΛΥΤΙΚΑ ΑΝΑ ΠΕΛΑΤΗ:")
            appendLine("-" * 50)

            if (payroll.clientBreakdown.isNotEmpty()) {
                payroll.clientBreakdown.forEach { client ->
                    appendLine()
                    appendLine("👤 Πελάτης: ${client.clientName}")
                    appendLine("   Συνεδρίες: ${client.sessions}")
                    appendLine("   Τιμή/Συνεδρία: €${client.pricePerSession}")
                    appendLine("   Μερίδιο Εργαζομένου: €${client.employeePricePerSession}")
                    appendLine("   Μερίδιο Εταιρίας: €${client.companyPricePerSession}")
                    appendLine("   Συνολικά Έσοδα: €${client.totalRevenue}")
                    appendLine("   Κέρδη Εργαζομένου: €${client.employeeEarnings}")
                    appendLine("   Κέρδη Εταιρίας: €${client.companyEarnings}")

                    if (client.eventDetails.isNotEmpty()) {
                        appendLine("   Συνεδρίες:")
                        client.eventDetails.forEach { event ->
                            val statusIcon = when (event.status) {
                                "completed" -> "✅"
                                "cancelled" -> "❌"
                                "pending_payment" -> "⏳"
                                else -> "📅"
                            }
                            appendLine("     $statusIcon ${event.date} ${event.time} (${event.duration})")
                        }
                    }
                    appendLine("   " + "-" * 40)
                }
            } else {
                appendLine("Δεν βρέθηκαν συνεδρίες για την επιλεγμένη περίοδο.")
            }

            appendLine()
            appendLine("=" * 80)
            appendLine("Δημιουργήθηκε από το Σύστημα Μισθοδοσίας")
            appendLine("${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}")
            appendLine("=" * 80)
        }
    }

    private fun generateCsvContent(payroll: PayrollResponse): String {
        return buildString {
            appendLine("Αναφορά Μισθοδοσίας")
            appendLine("Εργαζόμενος,${payroll.employee.name}")
            appendLine("Email,${payroll.employee.email}")
            appendLine("Περίοδος,${payroll.period}")
            appendLine("Δημιουργήθηκε,${payroll.generatedAt}")
            appendLine()

            appendLine("ΣΥΓΚΕΝΤΡΩΤΙΚΑ")
            appendLine("Μέτρηση,Αξία")
            appendLine("Συνολικές Συνεδρίες,${payroll.summary.totalSessions}")
            appendLine("Συνολικά Έσοδα,${payroll.summary.totalRevenue}")
            appendLine("Μισθός Εργαζομένου,${payroll.summary.employeeEarnings}")
            appendLine("Κέρδη Εταιρίας,${payroll.summary.companyEarnings}")
            appendLine()

            appendLine("ΑΝΑΛΥΤΙΚΑ ΑΝΑ ΠΕΛΑΤΗ")
            appendLine("Πελάτης,Συνεδρίες,Τιμή/Συνεδρία,Μερίδιο Εργαζομένου,Μερίδιο Εταιρίας,Συνολικά Έσοδα,Κέρδη Εργαζομένου,Κέρδη Εταιρίας")

            payroll.clientBreakdown.forEach { client ->
                appendLine("${client.clientName},${client.sessions},${client.pricePerSession},${client.employeePricePerSession},${client.companyPricePerSession},${client.totalRevenue},${client.employeeEarnings},${client.companyEarnings}")
            }

            appendLine()
            appendLine("ΛΕΠΤΟΜΕΡΕΙΕΣ ΣΥΝΕΔΡΙΩΝ")
            appendLine("Πελάτης,Ημερομηνία,Ώρα,Διάρκεια,Κατάσταση")

            payroll.clientBreakdown.forEach { client ->
                client.eventDetails.forEach { event ->
                    appendLine("${client.clientName},${event.date},${event.time},${event.duration},${event.status}")
                }
            }
        }
    }
}

// String repeat helper
private operator fun String.times(n: Int): String = this.repeat(n)
