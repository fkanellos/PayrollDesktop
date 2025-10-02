package com.payroll.app.desktop.core.export

import com.payroll.app.desktop.domain.models.PayrollResponse

expect class ExportService() {
    fun exportToPdf(payrollResult: PayrollResponse): ExportResult
    fun exportToExcel(payrollResult: PayrollResponse): ExportResult
    fun savePdfBytes(pdfBytes: ByteArray, filename: String): ExportResult
}

sealed class ExportResult {
    data class Success(val filePath: String, val fileType: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
