package com.payroll.app.desktop.core.resources

/**
 * Type-safe string resources for passing messages from ViewModel → UI
 *
 * Architecture:
 * 1. ViewModel emits effect with StringMessage sealed class
 * 2. UI layer formats the message with toDisplayString()
 *
 * Example:
 * ```
 * // ViewModel
 * emitSideEffect(
 *     PayrollEffect.ShowToast(
 *         message = StringMessage.CalculationComplete(sessions, revenue)
 *     )
 * )
 *
 * // UI (LaunchedEffect)
 * val displayMessage = effect.message.toDisplayString()
 * snackbarHostState.showSnackbar(displayMessage)
 * ```
 */
sealed class StringMessage {
    // Simple messages (no args)
    object CreatingPdf : StringMessage()
    object CreatingExcel : StringMessage()
    object SyncingDatabase : StringMessage()
    object RefreshingData : StringMessage()
    object StartingCalculation : StringMessage()
    object AllMatchesConfirmed : StringMessage()
    object AllMatchesProcessed : StringMessage()
    object LargeDateRangeWarning : StringMessage()
    object RetryingCalculation : StringMessage()
    object RefreshComplete : StringMessage()

    // Messages with arguments
    data class EmployeesLoaded(val count: Int) : StringMessage()
    data class EmployeeSelectedWithClients(val name: String, val clientCount: Int) : StringMessage()
    data class UncertainMatchesFound(val count: Int) : StringMessage()
    data class CalculationComplete(val sessions: Int, val revenue: Double) : StringMessage()
    data class MatchConfirmed(val eventTitle: String, val clientName: String) : StringMessage()
    data class MatchRejected(val eventTitle: String) : StringMessage()
    data class ClientAddSuccess(val clientName: String, val price: Double, val employeePrice: Double, val companyPrice: Double) : StringMessage()
    data class PdfCreated(val filePath: String) : StringMessage()
    data class ExcelCreated(val filePath: String) : StringMessage()
    data class SyncComplete(val employeesInserted: Int, val employeesUpdated: Int, val clientsInserted: Int, val clientsUpdated: Int) : StringMessage()

    // Error messages
    data class CustomError(val message: String) : StringMessage()
}

/**
 * Convert StringMessage to display string
 * UI layer responsibility - formats the message with arguments
 */
fun StringMessage.toDisplayString(): String {
    return when (this) {
        // Simple messages
        is StringMessage.CreatingPdf -> "Δημιουργία PDF..."
        is StringMessage.CreatingExcel -> "Δημιουργία Excel..."
        is StringMessage.SyncingDatabase -> "Συγχρονισμός βάσης δεδομένων..."
        is StringMessage.RefreshingData -> "Ανανέωση δεδομένων..."
        is StringMessage.StartingCalculation -> "Έναρξη υπολογισμού μισθοδοσίας..."
        is StringMessage.AllMatchesConfirmed -> "Όλα τα matches επιβεβαιώθηκαν! Επανυπολογισμός..."
        is StringMessage.AllMatchesProcessed -> "Όλα τα matches επεξεργάστηκαν! Επανυπολογισμός..."
        is StringMessage.LargeDateRangeWarning -> "⚠️ Μεγάλο εύρος ημερομηνιών - ο υπολογισμός μπορεί να πάρει λίγο χρόνο"
        is StringMessage.RetryingCalculation -> "Επανάληψη υπολογισμού..."
        is StringMessage.RefreshComplete -> "Ανανέωση ολοκληρώθηκε!"

        // Messages with arguments
        is StringMessage.EmployeesLoaded -> "✅ Φορτώθηκαν $count εργαζόμενοι"
        is StringMessage.EmployeeSelectedWithClients -> "Επιλέχθηκε $name - $clientCount πελάτες"
        is StringMessage.UncertainMatchesFound -> "⚠️ Βρέθηκαν $count αβέβαιες αντιστοιχίες - επιβεβαιώστε τις"
        is StringMessage.CalculationComplete -> "✅ Υπολογισμός ολοκληρώθηκε! $sessions συνεδρίες, €%.2f".format(revenue)
        is StringMessage.MatchConfirmed -> "✅ Match επιβεβαιώθηκε: '$eventTitle' → '$clientName'"
        is StringMessage.MatchRejected -> "❌ Match απορρίφθηκε: '$eventTitle'"
        is StringMessage.ClientAddSuccess -> "✅ Πελάτης '$clientName' προστέθηκε (€%.2f, Εργαζόμενος: €%.2f, Εταιρία: €%.2f)".format(price, employeePrice, companyPrice)
        is StringMessage.PdfCreated -> "PDF δημιουργήθηκε!\nΣώθηκε: $filePath"
        is StringMessage.ExcelCreated -> "Excel δημιουργήθηκε!\nΣώθηκε: $filePath"
        is StringMessage.SyncComplete -> "✅ Συγχρονισμός ολοκληρώθηκε!\nΕργαζόμενοι: +$employeesInserted, ~$employeesUpdated | Πελάτες: +$clientsInserted, ~$clientsUpdated"

        // Custom error messages
        is StringMessage.CustomError -> message
    }
}
