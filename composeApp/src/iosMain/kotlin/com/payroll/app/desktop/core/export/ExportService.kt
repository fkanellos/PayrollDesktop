package com.payroll.app.desktop.core.export

import com.payroll.app.desktop.domain.models.PayrollResponse

actual class ExportService actual constructor() {

    actual fun exportToPdf(payrollResult: PayrollResponse): ExportResult {
        return ExportResult.Error("PDF export is not supported on iOS")
    }

    actual fun exportToExcel(payrollResult: PayrollResponse): ExportResult {
        return ExportResult.Error("Excel export is not supported on iOS")
    }

    actual fun savePdfBytes(pdfBytes: ByteArray, filename: String): ExportResult {
        return ExportResult.Error("PDF saving is not supported on iOS")
    }
}