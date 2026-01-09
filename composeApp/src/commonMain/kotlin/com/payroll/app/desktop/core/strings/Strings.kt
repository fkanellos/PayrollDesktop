package com.payroll.app.desktop.core.strings

/**
 * Centralized strings for the application
 * All UI text should be defined here for easy maintenance and future i18n support
 */
object Strings {

    // ==================== COMMON ====================
    object Common {
        const val appName = "Payroll Desktop"
        const val loading = "Φόρτωση..."
        const val refresh = "Ανανέωση"
        const val refreshing = "Ανανέωση..."
        const val save = "Αποθήκευση"
        const val cancel = "Ακύρωση"
        const val delete = "Διαγραφή"
        const val edit = "Επεξεργασία"
        const val close = "Κλείσιμο"
        const val back = "Πίσω"
        const val search = "Αναζήτηση"
        const val clear = "Καθαρισμός"
        const val confirm = "Επιβεβαίωση"
        const val yes = "Ναι"
        const val no = "Όχι"
        const val ok = "OK"
        const val error = "Σφάλμα"
        const val success = "Επιτυχία"
        const val warning = "Προειδοποίηση"
        const val info = "Πληροφορία"
        const val add = "+ Προσθήκη"
    }

    // ==================== NAVIGATION TABS ====================
    object Tabs {
        const val payroll = "💰 Μισθοδοσία"
        const val clientManagement = "👥 Διαχείριση Πελατών"
        const val employeeManagement = "👤 Διαχείριση Εργαζομένων"
        const val settings = "⚙️ Ρυθμίσεις"
    }

    // ==================== PAYROLL SCREEN ====================
    object Payroll {
        const val title = "Μισθοδοσία"
        const val subtitle = "Υπολογισμός εσόδων και αμοιβών"

        const val selectEmployee = "Επιλογή Εργαζομένου"
        const val selectEmployeePlaceholder = "Επιλέξτε εργαζόμενο..."
        const val startDate = "Ημερομηνία Έναρξης"
        const val endDate = "Ημερομηνία Λήξης"
        const val calculate = "Υπολογισμός"
        const val calculating = "Υπολογισμός..."
        const val export = "Εξαγωγή"
        const val exporting = "Εξαγωγή..."

        const val results = "Αποτελέσματα"
        const val totalSessions = "Συνολικές Συνεδρίες"
        const val totalRevenue = "Συνολικά Έσοδα"
        const val employeeEarnings = "Αμοιβή Εργαζομένου"
        const val companyEarnings = "Αμοιβή Εταιρείας"

        const val clientDetails = "Ανάλυση ανά Πελάτη"
        const val client = "Πελάτης"
        const val sessions = "Συνεδρίες"
        const val price = "Τιμή"
        const val employeeShare = "Αμοιβή Εργ."
        const val companyShare = "Αμοιβή Εταιρ."
        const val revenue = "Έσοδα"

        const val unmatchedEvents = "Μη Αντιστοιχισμένα Events"
        const val unmatchedCount = "Βρέθηκαν %d μη αντιστοιχισμένα events"
        const val quickAdd = "Γρήγορη Προσθήκη"
        const val quickAddAll = "Προσθήκη Όλων"
        const val confirmMatch = "Επιβεβαίωση"
        const val skipMatch = "Παράλειψη"
        const val viewAll = "Προβολή Όλων"

        const val noResults = "Δεν υπάρχουν αποτελέσματα"
        const val selectEmployeeFirst = "Παρακαλώ επιλέξτε εργαζόμενο και ημερομηνίες"
        const val noEmployees = "Δεν βρέθηκαν εργαζόμενοι"
        const val loadEmployees = "Φόρτωση εργαζομένων..."

        const val creatingPdf = "Δημιουργία PDF..."
        const val creatingExcel = "Δημιουργία Excel..."
        const val noResultsToExport = "Δεν υπάρχουν αποτελέσματα για εξαγωγή"
        const val syncingDatabase = "🔄 Συγχρονισμός βάσης δεδομένων από Google Sheets..."
        fun syncCompleteWithStats(employeesInserted: Int, employeesUpdated: Int, clientsInserted: Int, clientsUpdated: Int) = "✅ Συγχρονισμός ολοκληρώθηκε!\nΕργαζόμενοι: +$employeesInserted / ↻$employeesUpdated\nΠελάτες: +$clientsInserted / ↻$clientsUpdated"
        const val syncError = "Σφάλμα συγχρονισμού"
        fun employeeSelectedWithClients(name: String, count: Int) = "Επιλέχθηκε $name - $count πελάτες"
        fun employeeHasNoClients(name: String) = "Ο εργαζόμενος $name δεν έχει καταχωρημένους πελάτες"
        fun errorCheckingClients(name: String) = "Σφάλμα ελέγχου πελατών για $name"
        const val errorValidatingEmployee = "Σφάλμα επικύρωσης εργαζομένου"
        const val startDateMustBeBeforeEnd = "Η ημερομηνία έναρξης πρέπει να είναι πριν τη λήξη"
        const val startDateCannotBeFuture = "Η ημερομηνία έναρξης δεν μπορεί να είναι στο μέλλον"
        const val dateRangeExceedsMax = "Το διάστημα δεν μπορεί να υπερβαίνει το 1 έτος"
        const val largeDateRangeWarning = "⚠️ Μεγάλο χρονικό διάστημα - ο υπολογισμός ίσως αργήσει"
        const val pleaseSelectEmployee = "Παρακαλώ επιλέξτε εργαζόμενο"
        const val pleaseSelectStartDate = "Παρακαλώ επιλέξτε ημερομηνία έναρξης"
        const val pleaseSelectEndDate = "Παρακαλώ επιλέξτε ημερομηνία λήξης"
        const val calculationInProgress = "Υπολογισμός ήδη σε εξέλιξη"
        const val pleaseFillAllFields = "Παρακαλώ συμπληρώστε όλα τα απαραίτητα πεδία"
        const val startingCalculation = "Έναρξη υπολογισμού μισθοδοσίας..."
        fun uncertainMatchesFound(count: Int) = "⚠️ Βρέθηκαν $count αβέβαιες αντιστοιχίες που χρειάζονται επιβεβαίωση"
        fun calculationComplete(sessions: Int, amount: String) = "✅ Υπολογισμός ολοκληρώθηκε! $sessions συνεδρίες, $amount€"
        const val calculationError = "Σφάλμα υπολογισμού"
        fun pdfCreated(path: String) = "✅ PDF δημιουργήθηκε: $path"
        const val pdfExportError = "Σφάλμα εξαγωγής PDF"
        fun excelCreated(path: String) = "✅ Excel δημιουργήθηκε: $path"
        const val excelExportError = "Σφάλμα εξαγωγής Excel"
        const val sheetsSyncNotImplemented = "Sheets sync not yet implemented for local mode"
        fun matchConfirmed(from: String, to: String) = "✅ Αποθηκεύτηκε: '$from' → '$to'"
        const val allMatchesConfirmed = "Όλες οι αντιστοιχίες επιβεβαιώθηκαν! Επαναυπολογισμός..."
        fun matchRejected(name: String) = "Απορρίφθηκε: '$name'"
        const val allMatchesProcessed = "Όλες οι αντιστοιχίες επεξεργάστηκαν! Επαναυπολογισμός..."
        fun clientAddSuccess(name: String, total: String, employee: String, company: String) = "✅ Πελάτης '$name' προστέθηκε! (€$total: Εργαζόμενος €$employee / Εταιρία €$company)"
        const val clientAddFailed = "Αποτυχία προσθήκης πελάτη"
        const val refreshingData = "🔄 Ανανέωση δεδομένων..."
        const val retryingCalculation = "🔄 Επανάληψη υπολογισμού..."
        const val refreshComplete = "✅ Ανανέωση ολοκληρώθηκε"
        const val refreshError = "Σφάλμα ανανέωσης"

        // Unmatched events messages
        const val allUnmatchedAdded = "Όλοι οι μη αντιστοιχισμένοι πελάτες προστέθηκαν! Επανυπολογίστε για να δείτε τα αποτελέσματα."
        const val clickToAddClient = "Πατήστε + για να προσθέσετε πελάτη με προεπιλεγμένες τιμές"
        const val defaultPrices = "Προεπιλεγμένες τιμές:"
        const val defaultPriceTotal = "Σύνολο: €50"
        const val defaultPriceEmployee = "Εργαζόμενος: €22.5"
        const val defaultPriceCompany = "Εταιρία: €27.5"

        // Client status and payment labels
        fun includesPendingPayments(amount: String, count: Int) = "Περιλαμβάνει $amount από $count εκκρεμείς πληρωμές"
        fun clientOwes(count: Int) = "Ο πελάτης χρωστάει $count εκκρεμείς πληρωμές"
        const val pendingPayment = "Εκκρεμής Πληρωμή"
        const val statusCompleted = "Ολοκληρωμένη"
        const val statusCancelled = "Ακυρωμένη"
        fun sessionsLabel(count: Int) = "Συνεδρίες: $count"
        fun paidLabel(amount: String) = "Πληρωμένες: $amount"
        fun completedSessionsLabel(count: Int) = "Ολοκληρωμένες: $count"
        fun pendingSessionsLabel(count: Int) = "Εκκρεμείς: $count"
        fun paidPreviouslyLabel(count: Int) = "Προπληρωμένες: $count"
        fun unmatchedEventsLabel(count: Int) = "Μη Αντιστοιχισμένα Συμβάντα ($count)"
    }

    // ==================== CLIENT MANAGEMENT ====================
    object ClientManagement {
        const val title = "Διαχείριση Πελατών"
        const val subtitle = "Διαχείριση πελατών ανά εργαζόμενο"

        const val selectEmployee = "Επιλέξτε εργαζόμενο"
        const val searchClients = "Αναζήτηση πελατών..."
        const val addClient = "Προσθήκη Πελάτη"
        const val editClient = "Επεξεργασία Πελάτη"
        const val deleteClient = "Διαγραφή Πελάτη"
        const val syncClients = "Συγχρονισμός"
        const val syncing = "Συγχρονισμός..."

        const val clientName = "Όνομα Πελάτη"
        const val clientPrice = "Τιμή"
        const val employeePrice = "Τιμή Εργαζομένου"
        const val companyPrice = "Τιμή Εταιρείας"

        fun totalClients(count: Int) = "Σύνολο: $count πελάτες"
        const val noClients = "Δεν υπάρχουν πελάτες"
        const val noClientsForEmployee = "Δεν υπάρχουν πελάτες για αυτόν τον εργαζόμενο"

        const val confirmDelete = "Επιβεβαίωση Διαγραφής"
        fun confirmDeleteMessage(name: String) = "Είστε σίγουροι ότι θέλετε να διαγράψετε τον πελάτη '$name';"
    }

    // ==================== EMPLOYEE MANAGEMENT ====================
    object EmployeeManagement {
        const val title = "Διαχείριση Εργαζομένων"
        const val subtitle = "Διαχείριση στοιχείων εργαζομένων"

        const val addEmployee = "Προσθήκη Εργαζομένου"
        const val editEmployee = "Επεξεργασία Εργαζομένου"
        const val deleteEmployee = "Διαγραφή Εργαζομένου"

        const val employeeName = "Όνομα"
        const val email = "Email"
        const val calendarId = "Calendar ID"
        const val sheetName = "Sheet Name"
        const val supervisionPrice = "Τιμή Εποπτείας (€)"
        const val color = "Χρώμα"

        const val noEmployees = "Δεν υπάρχουν εργαζόμενοι"
        const val confirmDelete = "Επιβεβαίωση Διαγραφής"
        fun confirmDeleteMessage(name: String) = "Είστε σίγουροι ότι θέλετε να διαγράψετε τον εργαζόμενο '$name';"
    }

    // ==================== SETTINGS ====================
    object Settings {
        const val title = "Ρυθμίσεις"
        const val dataSync = "Συγχρονισμός Δεδομένων"
        const val dataSyncDescription = "Χρησιμοποιήστε αυτά τα κουμπιά σε περίπτωση ανάγκης. Κανονικά, οι αλλαγές συγχρονίζονται αυτόματα."

        const val syncFromSheets = "Συγχρονισμός από Google Sheets"
        const val syncFromSheetsDesc = "Φόρτωση δεδομένων από το Google Sheet στη βάση δεδομένων. Ενημερώνει εργαζόμενους και πελάτες με τις τιμές από το sheet."
        const val syncFromSheetsButton = "Συγχρονισμός"
        const val syncingFromSheets = "Φόρτωση..."

        const val pushToSheets = "Αποστολή στο Google Sheets"
        const val pushToSheetsDesc = "Αποστολή όλων των δεδομένων από τη βάση στο Google Sheet. Χρήσιμο αν προσθέσατε δεδομένα πριν ενεργοποιηθεί το auto-update."
        const val pushToSheetsButton = "Αποστολή"
        const val pushingToSheets = "Φόρτωση..."

        const val syncStarting = "Ξεκινά συγχρονισμός από Google Sheets..."
        const val pushStarting = "Ξεκινά push στο Google Sheets..."
    }

    // ==================== VALIDATION MESSAGES ====================
    object Validation {
        const val required = "Υποχρεωτικό πεδίο"
        const val invalidEmail = "Μη έγκυρο email"
        const val invalidNumber = "Μη έγκυρος αριθμός"
        const val invalidDate = "Μη έγκυρη ημερομηνία"
        const val endDateBeforeStart = "Η ημερομηνία λήξης πρέπει να είναι μετά την έναρξη"
        const val nameRequired = "Το όνομα είναι υποχρεωτικό"
        const val emailRequired = "Το email είναι υποχρεωτικό"
        const val pricePositive = "Η τιμή πρέπει να είναι θετική"
        const val pricesMustMatch = "Η τιμή εργαζομένου + εταιρείας πρέπει να ισούται με τη συνολική τιμή"

        // Employee validation
        const val employeeNameRequired = "Το όνομα εργαζόμενου είναι υποχρεωτικό"
        const val supervisionPriceNegative = "Η τιμή supervision δεν μπορεί να είναι αρνητική"
        const val supervisionPriceInvalid = "Η τιμή supervision περιέχει μη έγκυρη τιμή"
        fun supervisionPriceExceedsMax(maxPrice: String) = "Η τιμή supervision δεν μπορεί να υπερβαίνει €$maxPrice"
        fun employeeEmailDuplicate(email: String) = "Υπάρχει ήδη εργαζόμενος με το email '$email'"

        // Client validation
        const val clientNameRequired = "Το όνομα πελάτη είναι υποχρεωτικό"
        const val clientPriceNegative = "Η τιμή πελάτη δεν μπορεί να είναι αρνητική"
        const val employeePriceNegative = "Η τιμή εργαζόμενου δεν μπορεί να είναι αρνητική"
        const val companyPriceNegative = "Η τιμή εταιρίας δεν μπορεί να είναι αρνητική"
        const val clientPriceInvalid = "Η τιμή πελάτη περιέχει μη έγκυρη τιμή"
        const val employeePriceInvalid = "Η τιμή εργαζόμενου περιέχει μη έγκυρη τιμή"
        const val companyPriceInvalid = "Η τιμή εταιρίας περιέχει μη έγκυρη τιμή"
        fun clientPriceExceedsMax(maxPrice: String) = "Η τιμή πελάτη δεν μπορεί να υπερβαίνει €$maxPrice"
        fun employeePriceExceedsMax(maxPrice: String) = "Η τιμή εργαζόμενου δεν μπορεί να υπερβαίνει €$maxPrice"
        fun companyPriceExceedsMax(maxPrice: String) = "Η τιμή εταιρίας δεν μπορεί να υπερβαίνει €$maxPrice"
        fun employeePriceExceedsTotal(employeePrice: String, totalPrice: String) = "Η τιμή εργαζόμενου ($employeePrice€) δεν μπορεί να ξεπερνά την τιμή πελάτη ($totalPrice€)"
        fun companyPriceExceedsTotal(companyPrice: String, totalPrice: String) = "Η τιμή εταιρίας ($companyPrice€) δεν μπορεί να ξεπερνά την τιμή πελάτη ($totalPrice€)"
        fun pricesMismatch(employeePrice: String, companyPrice: String, totalPrice: String, sum: String) = "Το άθροισμα τιμής εργαζόμενου ($employeePrice€) και εταιρίας ($companyPrice€) πρέπει να ισούται με την τιμή πελάτη ($totalPrice€). Τρέχον άθροισμα: $sum€"
        fun clientNameDuplicate(name: String) = "Υπάρχει ήδη πελάτης με το όνομα '$name' για αυτόν τον εργαζόμενο"

        // Validation Utils error messages
        fun fieldEmpty(fieldName: String) = "Το πεδίο '$fieldName' δεν μπορεί να είναι κενό"
        fun fieldMustBeNumber(fieldName: String) = "Το πεδίο '$fieldName' πρέπει να είναι αριθμός"
        fun fieldMinValue(fieldName: String, minValue: String) = "Το πεδίο '$fieldName' πρέπει να είναι τουλάχιστον €$minValue"
        fun fieldMaxValue(fieldName: String, maxValue: String) = "Το πεδίο '$fieldName' δεν μπορεί να υπερβαίνει €$maxValue"
        fun fieldInvalidValue(fieldName: String) = "Το πεδίο '$fieldName' περιέχει μη έγκυρη τιμή"
        const val emailEmpty = "Το email δεν μπορεί να είναι κενό"
        const val emailInvalidFormat = "Μη έγκυρη μορφή email"
        fun priceSumMismatch(sum: String, totalPrice: String) = "Το άθροισμα των τιμών (€$sum) δεν ταιριάζει με τη συνολική τιμή (€$totalPrice)"
    }

    // ==================== FIELD NAMES ====================
    object Fields {
        const val generic = "Πεδίο"
        const val price = "Τιμή"
        const val sessionPrice = "Τιμή Συνεδρίας"
        const val employeePrice = "Τιμή Εργαζομένου"
        const val companyPrice = "Τιμή Εταιρείας"
        const val supervisionPrice = "Τιμή Supervision"
    }

    // ==================== BUSINESS LOGIC KEYWORDS ====================
    object Keywords {
        const val supervision = "εποπτεια"  // Normalized Greek word for supervision (used in calendar event matching)
        const val supervisionEnglish = "supervision"
    }

    // ==================== ERROR MESSAGES ====================
    object Errors {
        const val generic = "Παρουσιάστηκε σφάλμα"
        const val networkError = "Σφάλμα δικτύου"
        const val notFound = "Δεν βρέθηκε"
        const val unauthorized = "Μη εξουσιοδοτημένη πρόσβαση"
        const val serverError = "Σφάλμα διακομιστή"
        const val timeout = "Λήξη χρόνου αναμονής"

        const val loadEmployeesFailed = "Αποτυχία φόρτωσης εργαζομένων"
        const val loadClientsFailed = "Αποτυχία φόρτωσης πελατών"
        const val calculateFailed = "Αποτυχία υπολογισμού"
        const val saveFailed = "Αποτυχία αποθήκευσης"
        const val deleteFailed = "Αποτυχία διαγραφής"
        const val exportFailed = "Αποτυχία εξαγωγής"
        fun syncFailed(message: String) = "Αποτυχία συγχρονισμού: $message"
        fun pushFailed(message: String) = "Αποτυχία push: $message"

        const val noCalendarEvents = "Δεν βρέθηκαν events στο ημερολόγιο"
        const val invalidDateRange = "Μη έγκυρο εύρος ημερομηνιών"
    }

    // ==================== SUCCESS MESSAGES ====================
    object Success {
        const val saved = "Αποθηκεύτηκε επιτυχώς"
        const val deleted = "Διαγράφηκε επιτυχώς"
        const val exported = "Εξήχθη επιτυχώς"
        const val synced = "Συγχρονίστηκε επιτυχώς"

        const val clientAdded = "Ο πελάτης προστέθηκε"
        const val clientUpdated = "Ο πελάτης ενημερώθηκε"
        const val clientDeleted = "Ο πελάτης διαγράφηκε"

        const val employeeAdded = "Ο εργαζόμενος προστέθηκε"
        const val employeeUpdated = "Ο εργαζόμενος ενημερώθηκε"
        const val employeeDeleted = "Ο εργαζόμενος διαγράφηκε"

        fun syncComplete(employeesInserted: Int, employeesUpdated: Int, clientsInserted: Int, clientsUpdated: Int, durationSec: String) = "📥 Συγχρονισμός ολοκληρώθηκε!\n\nΕργαζόμενοι: +$employeesInserted / ↻$employeesUpdated\nΠελάτες: +$clientsInserted / ↻$clientsUpdated\n\n⏱️ Διάρκεια: $durationSec δευτερόλεπτα"
        fun pushComplete(employeesPushed: Int, clientsPushed: Int, durationSec: String) = "🚀 Push ολοκληρώθηκε!\n\nΕργαζόμενοι: ✓$employeesPushed\nΠελάτες: ✓$clientsPushed\n\n⏱️ Διάρκεια: $durationSec δευτερόλεπτα"
        fun pushCompleteWithErrors(employeesPushed: Int, employeesFailed: Int, clientsPushed: Int, clientsFailed: Int, durationSec: String) = "🚀 Push ολοκληρώθηκε!\n\nΕργαζόμενοι: ✓$employeesPushed / ✗$employeesFailed\nΠελάτες: ✓$clientsPushed / ✗$clientsFailed\n\n⏱️ Διάρκεια: $durationSec δευτερόλεπτα"

        fun syncResult(count: Int) = "Συγχρονισμός: $count χωρίς αλλαγές"
        fun syncResultWithChanges(newCount: Int, updatedCount: Int) = "Συγχρονισμός: +$newCount νέοι, ↻$updatedCount ενημερώθηκαν"

        fun employeesLoaded(count: Int) = "Φορτώθηκαν $count εργαζόμενοι"
        fun clientsLoaded(count: Int) = "Φορτώθηκαν $count πελάτες"
    }

    // ==================== INFO MESSAGES ====================
    object Info {
        const val refreshingData = "🔄 Ανανέωση δεδομένων..."
        const val retryingCalculation = "🔄 Επανάληψη υπολογισμού..."
        const val loadingEmployees = "Φόρτωση εργαζομένων..."
        const val loadingClients = "Φόρτωση πελατών..."
        const val calculating = "Υπολογισμός..."
        const val exporting = "Εξαγωγή αρχείου..."
        const val syncing = "Συγχρονισμός..."
    }

    // ==================== DIALOG TITLES ====================
    object DialogTitles {
        const val addClient = "Προσθήκη Νέου Πελάτη"
        const val editClient = "Επεξεργασία Πελάτη"
        const val deleteClient = "Διαγραφή Πελάτη"

        const val addEmployee = "Προσθήκη Νέου Εργαζομένου"
        const val editEmployee = "Επεξεργασία Εργαζομένου"
        const val deleteEmployee = "Διαγραφή Εργαζομένου"

        const val confirmMatch = "Επιβεβαίωση Αντιστοίχισης"
        const val quickAddClient = "Γρήγορη Προσθήκη Πελάτη"
        const val unmatchedEvents = "Μη Αντιστοιχισμένα Events"
    }

    // ==================== CALENDAR & EVENTS ====================
    object Calendar {
        const val supervision = "Εποπτεία (Supervision)"
        const val event = "Event"
        const val cancelled = "Ακυρωμένο"
        const val pending = "Εκκρεμεί"
        const val confirmed = "Επιβεβαιωμένο"
    }

    // ==================== FORMATTING ====================
    object Format {
        const val currency = "€%.2f"
        const val currencyShort = "€%d"
        const val sessions = "%d συνεδρίες"
        const val sessionsSingular = "1 συνεδρία"
        const val date = "dd/MM/yyyy"
        const val dateTime = "dd/MM/yyyy HH:mm"
        const val duration = "%.1f δευτερόλεπτα"
        const val durationMinutes = "%.1f λεπτά"
    }
}
