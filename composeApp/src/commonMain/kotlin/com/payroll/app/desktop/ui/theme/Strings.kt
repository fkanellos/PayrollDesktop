package com.payroll.app.desktop.ui.theme

/**
 * Centralized strings for the Payroll App
 * All UI text should be referenced from here for consistency and easy localization
 */
object Strings {

    // ============================================================
    // COMMON / GENERAL
    // ============================================================
    object Common {
        const val APP_NAME = "Σύστημα Μισθοδοσίας"
        const val LOADING = "Φόρτωση..."
        const val SAVING = "Αποθήκευση..."
        const val ERROR = "Σφάλμα"
        const val SUCCESS = "Επιτυχία"
        const val WARNING = "Προειδοποίηση"
        const val CLOSE = "Κλείσιμο"
        const val CANCEL = "Ακύρωση"
        const val SAVE = "Αποθήκευση"
        const val DELETE = "Διαγραφή"
        const val EDIT = "Επεξεργασία"
        const val ADD = "Προσθήκη"
        const val SEARCH = "Αναζήτηση..."
        const val NO_DATA = "Δεν υπάρχουν δεδομένα"
        const val YES = "Ναι"
        const val NO = "Όχι"
        const val OK = "OK"
        const val RETRY = "Επανάληψη"
        const val SELECT = "Επιλογή"
        const val REQUIRED_FIELD = "* Υποχρεωτικό πεδίο"
    }

    // ============================================================
    // NAVIGATION / TABS
    // ============================================================
    object Navigation {
        const val TAB_PAYROLL = "Μισθοδοσία"
        const val TAB_CLIENTS = "Διαχείριση Πελατών"
        const val TAB_EMPLOYEES = "Διαχείριση Εργαζομένων"
        const val TAB_PAYROLL_ICON = "💰"
        const val TAB_CLIENTS_ICON = "👥"
        const val TAB_EMPLOYEES_ICON = "👤"
    }

    // ============================================================
    // PAYROLL SCREEN
    // ============================================================
    object Payroll {
        const val TITLE = "Σύστημα Μισθοδοσίας"
        const val SUBTITLE = "Αυτοματοποιημένος υπολογισμός μισθών από Google Calendar"

        // Form
        const val CALCULATION_TITLE = "Υπολογισμός Μισθοδοσίας"
        const val CALCULATION_SUBTITLE = "Επιλέξτε εργαζόμενο και περίοδο"
        const val CALCULATE = "Υπολογισμός Μισθοδοσίας"
        const val CALCULATING = "Υπολογισμός..."
        const val CALCULATE_ICON = "🔄"

        // Employee Selection
        const val SELECT_EMPLOYEE = "Εργαζόμενος"
        const val SELECT_EMPLOYEE_PLACEHOLDER = "Επιλέξτε εργαζόμενο..."
        const val NO_EMPLOYEES = "Δεν βρέθηκαν εργαζόμενοι"
        const val EMPLOYEE_SELECTED = "Επιλεγμένος"

        // Period Selection
        const val QUICK_PERIOD_SELECTION = "Γρήγορη Επιλογή Περιόδου"
        const val CUSTOM_DATES = "Προσαρμοσμένες Ημερομηνίες"
        const val CUSTOM_DATES_ICON = "📅"
        const val CUSTOM_DATES_ACTIVE = "Ενεργές"
        const val CUSTOM_DATES_HINT = "Επιλέξτε συγκεκριμένες ημερομηνίες"
        const val CUSTOM_DATES_TIP = "💡 Επιλέξτε συγκεκριμένες ημερομηνίες για προσαρμοσμένη περίοδο"
        const val DATE_FROM = "Από (Ημερομηνία)"
        const val DATE_TO = "Έως (Ημερομηνία)"
        const val SELECT_DATE = "Επιλέξτε ημερομηνία"
        const val DATE_PICKER_START = "Επιλογή Ημερομηνίας Έναρξης"
        const val DATE_PICKER_END = "Επιλογή Ημερομηνίας Λήξης"

        // Periods
        const val PERIOD_TWO_WORK_WEEKS = "2 Εργάσιμες Εβδομάδες"
        const val PERIOD_THIS_WEEK = "Τρέχουσα Εβδομάδα"
        const val PERIOD_LAST_WEEK = "Προηγούμενη Εβδομάδα"
        const val PERIOD_LAST_TWO_WEEKS = "Τελευταίες 2 Εβδομάδες"
        const val PERIOD_THIS_MONTH = "Τρέχων Μήνας"
        const val PERIOD_LAST_MONTH = "Προηγούμενος Μήνας"
        const val PERIOD_LAST_10_DAYS = "Τελευταίες 10 Εργάσιμες"
        const val PERIOD_TODAY = "Σήμερα"
        const val PERIOD_PREDEFINED = "Προκαθορισμένη περίοδος"
        const val PERIOD_CUSTOM = "Προσαρμοσμένη περίοδος"

        // Results
        const val RESULTS_TITLE = "Συγκεντρωτικά Αποτελέσματα"
        const val TOTAL_SESSIONS = "Συνολικές Συνεδρίες"
        const val TOTAL_REVENUE = "Συνολικά Έσοδα"
        const val EMPLOYEE_EARNINGS = "Μισθός Εργαζομένου"
        const val COMPANY_EARNINGS = "Κέρδη Εταιρίας"
        const val EMPLOYEE_SHARE = "Μερίδιο εργαζομένου"

        // Client Breakdown
        const val CLIENT_BREAKDOWN_TITLE = "Αναλυτικά Στοιχεία ανά Πελάτη"
        const val CLIENT_BREAKDOWN_SUBTITLE = "Λεπτομερής ανάλυση συνεδριών και εσόδων"
        const val PRICE_PER_SESSION = "Τιμή/Συνεδρία"
        const val SESSIONS = "συνεδρίες"
        const val NO_SESSIONS = "Δεν βρέθηκαν συνεδρίες"
        const val NO_SESSIONS_HINT = "Ελέγξτε αν υπάρχουν εγγραφές στο Google Calendar για την επιλεγμένη περίοδο"
        const val MORE_SESSIONS = "... και %d ακόμη συνεδρίες"
        const val CLIENT_TOTAL_REVENUE = "Συνολικά έσοδα πελάτη"

        // Export
        const val EXPORT_TITLE = "Εξαγωγή Αποτελεσμάτων"
        const val EXPORT_PDF = "Εξαγωγή PDF"
        const val EXPORT_PDF_ICON = "📄"
        const val EXPORT_EXCEL = "Εξαγωγή Excel"
        const val EXPORT_EXCEL_ICON = "📊"
        const val SEND_EMAIL = "Αποστολή Email"
        const val SEND_EMAIL_ICON = "📧"

        // Loading
        const val LOADING_MESSAGE = "Υπολογισμός σε εξέλιξη..."
        const val LOADING_ICON = "🔄"
        const val LOADING_SUBTITLE = "Ανάκτηση δεδομένων από Google Calendar"

        // Sync
        const val SYNC_DATABASE = "Συγχρονισμός Βάσης"
        const val SYNC_DATABASE_ICON = "🔄"
        const val SYNC_IN_PROGRESS = "Συγχρονισμός..."
        const val SYNC_SUCCESS = "Ο συγχρονισμός ολοκληρώθηκε"
        const val SYNC_ERROR = "Σφάλμα συγχρονισμού"

        // Sheets
        const val SHEETS_SEND = "Αποστολή σε Sheets"
        const val SHEETS_UPDATE = "Ενημέρωση Sheets"
        const val SHEETS_CONFIRM_SEND = "Αποστολή"
        const val SHEETS_CONFIRM_UPDATE = "Ενημέρωση"
        const val SHEETS_WARNING = "Τα παλιά δεδομένα θα διαγραφούν και θα αντικατασταθούν."
    }

    // ============================================================
    // EMPLOYEES
    // ============================================================
    object Employees {
        const val TITLE = "Εργαζόμενοι"
        const val SUBTITLE = "Διαχείριση εργαζομένων"
        const val ADD_EMPLOYEE = "Προσθήκη Εργαζομένου"
        const val EDIT_EMPLOYEE = "Επεξεργασία Εργαζομένου"
        const val DELETE_EMPLOYEE = "Διαγραφή Εργαζομένου"
        const val DELETE_CONFIRM = "Είστε σίγουροι ότι θέλετε να διαγράψετε τον εργαζόμενο '%s';"
        const val NO_EMPLOYEES = "Δεν υπάρχουν εργαζόμενοι"
        const val NO_EMPLOYEES_HINT = "Προσθέστε εργαζόμενους με το κουμπί παραπάνω"
        const val LOADING_EMPLOYEES = "Φόρτωση εργαζομένων..."
        const val SELECT_EMPLOYEE = "Επιλέξτε εργαζόμενο"

        // Form Fields
        const val FIELD_NAME = "Όνομα"
        const val FIELD_EMAIL = "Email"
        const val FIELD_CALENDAR_ID = "Calendar ID"
        const val FIELD_SHEET_NAME = "Sheet Name"
        const val FIELD_SUPERVISION_PRICE = "Τιμή Επίβλεψης (€)"
        const val FIELD_COLOR = "Χρώμα"
    }

    // ============================================================
    // CLIENTS
    // ============================================================
    object Clients {
        const val TITLE = "Πελάτες"
        const val SUBTITLE = "Διαχείριση πελατών"
        const val ADD_CLIENT = "Προσθήκη Πελάτη"
        const val EDIT_CLIENT = "Επεξεργασία Πελάτη"
        const val DELETE_CLIENT = "Διαγραφή Πελάτη"
        const val DELETE_CONFIRM = "Είστε σίγουροι ότι θέλετε να διαγράψετε τον πελάτη '%s';"
        const val NO_CLIENTS = "Δεν υπάρχουν πελάτες"
        const val NO_CLIENTS_HINT = "Προσθέστε πελάτες με το κουμπί παραπάνω"
        const val LOADING_CLIENTS = "Φόρτωση πελατών..."
        const val SHOWING_FOR = "Εμφάνιση πελατών για"

        // Table Headers
        const val HEADER_NAME = "Όνομα Πελάτη"
        const val HEADER_PRICE = "Τιμή"
        const val HEADER_EMPLOYEE = "Εργαζόμενος"
        const val HEADER_COMPANY = "Εταιρία"
        const val HEADER_ACTIONS = "Ενέργειες"

        // Form Fields
        const val FIELD_NAME = "Όνομα Πελάτη"
        const val FIELD_PRICE = "Τιμή Πελάτη (€)"
        const val FIELD_EMPLOYEE_PRICE = "Τιμή Εργαζόμενου (€)"
        const val FIELD_COMPANY_PRICE = "Τιμή Εταιρίας (€)"

        // Validation
        const val VALIDATION_SUM = "Άθροισμα"
        const val VALIDATION_EXPECTED = "Αναμενόμενο"
        const val VALIDATION_DIFFERENCE = "διαφορά"
    }

    // ============================================================
    // STATUS / SESSION STATES
    // ============================================================
    object Status {
        const val COMPLETED = "Ολοκληρώθηκε"
        const val CANCELLED = "Ακυρώθηκε"
        const val PENDING_PAYMENT = "Αναμονή πληρωμής"
        const val SCHEDULED = "Προγραμματισμένο"

        // Icons
        const val ICON_COMPLETED = "✅"
        const val ICON_CANCELLED = "❌"
        const val ICON_PENDING = "⏳"
        const val ICON_SCHEDULED = "📅"
        const val ICON_SUCCESS = "✅"
        const val ICON_ERROR = "❌"
        const val ICON_WARNING = "⚠️"
        const val ICON_INFO = "ℹ️"
    }

    // ============================================================
    // VALIDATION MESSAGES
    // ============================================================
    object Validation {
        const val NAME_REQUIRED = "Το όνομα είναι υποχρεωτικό"
        const val NAME_TOO_SHORT = "Το όνομα πρέπει να έχει τουλάχιστον 2 χαρακτήρες"
        const val NAME_TOO_LONG = "Το όνομα δεν μπορεί να υπερβαίνει τους 100 χαρακτήρες"
        const val NAME_DUPLICATE = "Υπάρχει ήδη πελάτης με αυτό το όνομα"
        const val PRICE_REQUIRED = "Η τιμή είναι υποχρεωτική"
        const val PRICE_INVALID = "Μη έγκυρη τιμή"
        const val PRICE_NEGATIVE = "Η τιμή δεν μπορεί να είναι αρνητική"
        const val EMAIL_INVALID = "Μη έγκυρο email"
        const val CALENDAR_ID_REQUIRED = "Το Calendar ID είναι υποχρεωτικό"
        const val DATE_START_REQUIRED = "Παρακαλώ επιλέξτε ημερομηνία έναρξης"
        const val DATE_END_REQUIRED = "Παρακαλώ επιλέξτε ημερομηνία λήξης"
        const val DATE_START_AFTER_END = "Η ημερομηνία έναρξης πρέπει να είναι πριν τη λήξη"
        const val DATE_IN_FUTURE = "Η ημερομηνία έναρξης δεν μπορεί να είναι στο μέλλον"
        const val DATE_RANGE_TOO_LARGE = "Το διάστημα δεν μπορεί να υπερβαίνει το 1 έτος"
        const val DATE_RANGE_WARNING = "Μεγάλο χρονικό διάστημα - ο υπολογισμός ίσως αργήσει"
    }

    // ============================================================
    // ERROR MESSAGES
    // ============================================================
    object Errors {
        const val NETWORK_ERROR = "Σφάλμα σύνδεσης. Ελέγξτε τη σύνδεση στο internet."
        const val SERVER_ERROR = "Σφάλμα διακομιστή. Παρακαλώ δοκιμάστε ξανά."
        const val UNKNOWN_ERROR = "Άγνωστο σφάλμα. Παρακαλώ επικοινωνήστε με την υποστήριξη."
        const val LOAD_EMPLOYEES_ERROR = "Σφάλμα φόρτωσης εργαζομένων"
        const val LOAD_CLIENTS_ERROR = "Σφάλμα φόρτωσης πελατών"
        const val SAVE_ERROR = "Σφάλμα αποθήκευσης"
        const val DELETE_ERROR = "Σφάλμα διαγραφής"
        const val CALCULATION_ERROR = "Σφάλμα υπολογισμού"
        const val EXPORT_ERROR = "Σφάλμα εξαγωγής"
    }

    // ============================================================
    // SYNC DATABASE
    // ============================================================
    object Sync {
        const val TITLE = "Συγχρονισμός Βάσης"
        const val ICON = "🔄"
        const val IN_PROGRESS = "Συγχρονισμός σε εξέλιξη..."
        const val SUCCESS = "Ο συγχρονισμός ολοκληρώθηκε επιτυχώς!"
        const val ERROR = "Σφάλμα συγχρονισμού"
        const val EMPLOYEES_INSERTED = "Εργαζόμενοι που προστέθηκαν"
        const val EMPLOYEES_UPDATED = "Εργαζόμενοι που ενημερώθηκαν"
        const val CLIENTS_INSERTED = "Πελάτες που προστέθηκαν"
        const val CLIENTS_UPDATED = "Πελάτες που ενημερώθηκαν"
        const val DURATION = "Διάρκεια"
        const val MS = "ms"
    }
}
