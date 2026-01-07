package com.payroll.app.desktop.domain.usecases

import com.payroll.app.desktop.core.export.ExportResult
import com.payroll.app.desktop.core.export.ExportService
import com.payroll.app.desktop.core.logging.Logger
import com.payroll.app.desktop.domain.models.PayrollResponse

/**
 * UseCase for exporting payroll results
 *
 * Responsibilities:
 * - Export to PDF
 * - Export to Excel
 * - Handle export errors with logging
 */
class PayrollExportUseCase(
    private val exportService: ExportService
) {
    companion object {
        private const val TAG = "PayrollExportUseCase"
    }

    /**
     * Export payroll to PDF
     */
    fun exportToPdf(payrollResponse: PayrollResponse): ExportResult {
        return try {
            Logger.info(TAG, "Exporting payroll to PDF")

            val result = exportService.exportToPdf(payrollResponse)

            when (result) {
                is ExportResult.Success -> {
                    Logger.info(TAG, "PDF exported successfully to: ${result.filePath}")
                }
                is ExportResult.Error -> {
                    Logger.error(TAG, "Failed to export PDF: ${result.message}")
                }
            }

            result
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error exporting PDF", e)
            ExportResult.Error(e.message ?: "Σφάλμα εξαγωγής PDF")
        }
    }

    /**
     * Export payroll to Excel
     */
    fun exportToExcel(payrollResponse: PayrollResponse): ExportResult {
        return try {
            Logger.info(TAG, "Exporting payroll to Excel")

            val result = exportService.exportToExcel(payrollResponse)

            when (result) {
                is ExportResult.Success -> {
                    Logger.info(TAG, "Excel exported successfully to: ${result.filePath}")
                }
                is ExportResult.Error -> {
                    Logger.error(TAG, "Failed to export Excel: ${result.message}")
                }
            }

            result
        } catch (e: Exception) {
            Logger.error(TAG, "Unexpected error exporting Excel", e)
            ExportResult.Error(e.message ?: "Σφάλμα εξαγωγής Excel")
        }
    }
}
