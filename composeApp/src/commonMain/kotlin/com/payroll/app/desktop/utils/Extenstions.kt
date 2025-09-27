package com.payroll.app.desktop.utils

import kotlinx.datetime.*

/**
 * Enhanced Date utility extensions με έμφαση σε εργάσιμες περιόδους
 */

fun Double.toEuroString(): String {
    val rounded = (this * 100).toInt() / 100.0
    return "€$rounded"
}

// Date formatting functions (unchanged)
fun LocalDateTime.toGreekDateString(): String {
    val day = this.dayOfMonth.toString().padStart(2, '0')
    val month = this.monthNumber.toString().padStart(2, '0')
    val year = this.year.toString()
    return "$day/$month/$year"
}

fun LocalDate.toGreekDateString(): String {
    val day = this.dayOfMonth.toString().padStart(2, '0')
    val month = this.monthNumber.toString().padStart(2, '0')
    val year = this.year.toString()
    return "$day/$month/$year"
}

fun LocalDateTime.toGreekDateTimeString(): String {
    val dateString = this.toGreekDateString()
    val hour = this.hour.toString().padStart(2, '0')
    val minute = this.minute.toString().padStart(2, '0')
    return "$dateString $hour:$minute"
}

// Date conversion helpers
fun Long.toLocalDate(): LocalDate {
    val instant = Instant.fromEpochMilliseconds(this)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun LocalDate.toEpochMillis(): Long {
    val dateTime = this.atTime(12, 0)
    return dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun LocalDateTime.toEpochMillis(): Long {
    return this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

// Work week helper functions
fun LocalDate.getStartOfWeek(): LocalDate {
    val dayOfWeek = this.dayOfWeek.isoDayNumber // Monday = 1, Sunday = 7
    val daysFromMonday = dayOfWeek - 1
    return this.minus(DatePeriod(days = daysFromMonday))
}

fun LocalDate.getEndOfWeek(): LocalDate {
    val dayOfWeek = this.dayOfWeek.isoDayNumber // Monday = 1, Sunday = 7
    val daysToFriday = 5 - dayOfWeek // Friday = 5
    return this.plus(DatePeriod(days = daysToFriday))
}

fun LocalDate.isWorkDay(): Boolean {
    return this.dayOfWeek != DayOfWeek.SATURDAY && this.dayOfWeek != DayOfWeek.SUNDAY
}

// Basic date functions (unchanged)
fun LocalDateTime.startOfDay(): LocalDateTime {
    return LocalDateTime(this.date, LocalTime(0, 0, 0))
}

fun LocalDateTime.endOfDay(): LocalDateTime {
    return LocalDateTime(this.date, LocalTime(23, 59, 59))
}

fun LocalDate.startOfMonth(): LocalDate {
    return LocalDate(this.year, this.month, 1)
}

fun LocalDate.endOfMonth(): LocalDate {
    val lastDayOfMonth = when (this.month) {
        Month.FEBRUARY -> if (this.year % 4 == 0 && (this.year % 100 != 0 || this.year % 400 == 0)) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
    return LocalDate(this.year, this.month, lastDayOfMonth)
}

// Enhanced DateRanges με εργάσιμες εβδομάδες
object DateRanges {

    /**
     * Επιστρέφει τη τρέχουσα εργάσιμη εβδομάδα (Δευτέρα-Παρασκευή)
     */
    fun thisWorkWeek(): Pair<LocalDateTime, LocalDateTime> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val monday = today.getStartOfWeek()
        val friday = today.getEndOfWeek()

        return Pair(
            LocalDateTime(monday, LocalTime(0, 0)),
            LocalDateTime(friday, LocalTime(23, 59, 59))
        )
    }

    /**
     * Επιστρέφει την προηγούμενη εργάσιμη εβδομάδα (Δευτέρα-Παρασκευή)
     */
    fun lastWorkWeek(): Pair<LocalDateTime, LocalDateTime> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val lastWeekDate = today.minus(DatePeriod(days = 7))
        val monday = lastWeekDate.getStartOfWeek()
        val friday = lastWeekDate.getEndOfWeek()

        return Pair(
            LocalDateTime(monday, LocalTime(0, 0)),
            LocalDateTime(friday, LocalTime(23, 59, 59))
        )
    }

    /**
     * 🎯 ΚΥΡΙΟ: Επιστρέφει 2 εργάσιμες εβδομάδες
     * (από Δευτέρα της προηγούμενης εβδομάδας μέχρι Παρασκευή της τρέχουσας)
     */
    fun twoWorkWeeks(): Pair<LocalDateTime, LocalDateTime> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Βρίσκουμε τη Δευτέρα της προηγούμενης εβδομάδας
        val lastWeekDate = today.minus(DatePeriod(days = 7))
        val startMonday = lastWeekDate.getStartOfWeek()

        // Βρίσκουμε την Παρασκευή της τρέχουσας εβδομάδας
        val endFriday = today.getEndOfWeek()

        return Pair(
            LocalDateTime(startMonday, LocalTime(0, 0)),
            LocalDateTime(endFriday, LocalTime(23, 59, 59))
        )
    }

    /**
     * Επιστρέφει τις τελευταίες 2 εργάσιμες εβδομάδες
     * (από Δευτέρα 2 εβδομάδες πριν μέχρι Παρασκευή της προηγούμενης εβδομάδας)
     */
    fun lastTwoWorkWeeks(): Pair<LocalDateTime, LocalDateTime> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Δευτέρα 2 εβδομάδες πριν
        val twoWeeksAgo = today.minus(DatePeriod(days = 14))
        val startMonday = twoWeeksAgo.getStartOfWeek()

        // Παρασκευή της προηγούμενης εβδομάδας
        val lastWeek = today.minus(DatePeriod(days = 7))
        val endFriday = lastWeek.getEndOfWeek()

        return Pair(
            LocalDateTime(startMonday, LocalTime(0, 0)),
            LocalDateTime(endFriday, LocalTime(23, 59, 59))
        )
    }

    // Κλασικές περίοδοι (unchanged αλλά improved)
    fun today(): Pair<LocalDateTime, LocalDateTime> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return Pair(now.startOfDay(), now.endOfDay())
    }

    fun yesterday(): Pair<LocalDateTime, LocalDateTime> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val yesterday = now.date.minus(DatePeriod(days = 1))
        val yesterdayDateTime = LocalDateTime(yesterday, LocalTime(0, 0))
        return Pair(yesterdayDateTime.startOfDay(), yesterdayDateTime.endOfDay())
    }

    fun thisMonth(): Pair<LocalDateTime, LocalDateTime> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val startOfMonth = LocalDateTime(now.date.startOfMonth(), LocalTime(0, 0))
        return Pair(startOfMonth, now.endOfDay())
    }

    fun lastMonth(): Pair<LocalDateTime, LocalDateTime> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val lastMonthDate = now.date.minus(DatePeriod(months = 1))
        val startOfLastMonth = LocalDateTime(lastMonthDate.startOfMonth(), LocalTime(0, 0))
        val endOfLastMonth = LocalDateTime(lastMonthDate.endOfMonth(), LocalTime(23, 59, 59))
        return Pair(startOfLastMonth, endOfLastMonth)
    }

    /**
     * Επιστρέφει τις τελευταίες X εργάσιμες μέρες
     */
    fun lastWorkDays(count: Int): Pair<LocalDateTime, LocalDateTime> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var workDaysFound = 0
        var currentDate = today
        var startDate = today

        while (workDaysFound < count) {
            if (currentDate.isWorkDay()) {
                workDaysFound++
                if (workDaysFound == count) {
                    startDate = currentDate
                }
            }
            currentDate = currentDate.minus(DatePeriod(days = 1))
        }

        return Pair(
            LocalDateTime(startDate, LocalTime(0, 0)),
            LocalDateTime(today, LocalTime(23, 59, 59))
        )
    }
}

// Validation functions
fun validateDateRange(startDate: LocalDateTime?, endDate: LocalDateTime?): String? {
    return when {
        startDate == null -> "Παρακαλώ επιλέξτε ημερομηνία έναρξης"
        endDate == null -> "Παρακαλώ επιλέξτε ημερομηνία λήξης"
        startDate >= endDate -> "Η ημερομηνία έναρξης πρέπει να είναι πριν τη λήξη"
        startDate > Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) ->
            "Η ημερομηνία έναρξης δεν μπορεί να είναι στο μέλλον"
        else -> {
            val daysDifference = endDate.date.toEpochDays() - startDate.date.toEpochDays()
            when {
                daysDifference > 365 -> "Το διάστημα δεν μπορεί να υπερβαίνει το 1 έτος"
                daysDifference > 90 -> "warning:Μεγάλο χρονικό διάστημα - ο υπολογισμός ίσως αργήσει"
                else -> null
            }
        }
    }
}

// Helper για να δούμε τι περίοδο παίρνουμε (for debugging)
fun Pair<LocalDateTime, LocalDateTime>.toReadableString(): String {
    return "${first.toGreekDateString()} - ${second.toGreekDateString()}"
}