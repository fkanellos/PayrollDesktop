package com.payroll.app.desktop.utils

import kotlinx.datetime.*

/**
 * Date utility extensions για καλύτερο formatting και χειρισμό ημερομηνιών
 * Fixed για kotlinx-datetime API
 */

fun Double.toEuroString(): String {
    val rounded = (this * 100).toInt() / 100.0
    return "€$rounded"
}

fun LocalDateTime.toGreekDateString(): String {
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

fun LocalDate.toGreekString(): String {
    val day = this.dayOfMonth.toString().padStart(2, '0')
    val month = this.monthNumber.toString().padStart(2, '0')
    val year = this.year.toString()
    return "$day/$month/$year"
}

fun LocalDate.getMonthNameInGreek(): String {
    return when (this.month) {
        Month.JANUARY -> "Ιανουάριος"
        Month.FEBRUARY -> "Φεβρουάριος"
        Month.MARCH -> "Μάρτιος"
        Month.APRIL -> "Απρίλιος"
        Month.MAY -> "Μάιος"
        Month.JUNE -> "Ιούνιος"
        Month.JULY -> "Ιούλιος"
        Month.AUGUST -> "Αύγουστος"
        Month.SEPTEMBER -> "Σεπτέμβριος"
        Month.OCTOBER -> "Οκτώβριος"
        Month.NOVEMBER -> "Νοέμβριος"
        else -> "Δεκέμβριος"
    }
}

fun LocalDate.getDayNameInGreek(): String {
    return when (this.dayOfWeek) {
        DayOfWeek.MONDAY -> "Δευτέρα"
        DayOfWeek.TUESDAY -> "Τρίτη"
        DayOfWeek.WEDNESDAY -> "Τετάρτη"
        DayOfWeek.THURSDAY -> "Πέμπτη"
        DayOfWeek.FRIDAY -> "Παρασκευή"
        DayOfWeek.SATURDAY -> "Σάββατο"
        else -> "Κυριακή"
    }
}

// Helper functions για date range calculations
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
    // Fix: Σωστή χρήση του kotlinx-datetime API
    val lastDayOfMonth = when (this.month) {
        Month.FEBRUARY -> if (this.year % 4 == 0 && (this.year % 100 != 0 || this.year % 400 == 0)) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
    return LocalDate(this.year, this.month, lastDayOfMonth)
}

// Quick date range generators
object DateRanges {
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

    fun lastWeek(): Pair<LocalDateTime, LocalDateTime> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val weekAgo = now.date.minus(DatePeriod(days = 7))
        val weekAgoDateTime = LocalDateTime(weekAgo, LocalTime(0, 0))
        return Pair(weekAgoDateTime.startOfDay(), now.endOfDay())
    }

    fun lastTwoWeeks(): Pair<LocalDateTime, LocalDateTime> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val twoWeeksAgo = now.date.minus(DatePeriod(days = 14))
        val twoWeeksAgoDateTime = LocalDateTime(twoWeeksAgo, LocalTime(0, 0))
        return Pair(twoWeeksAgoDateTime.startOfDay(), now.endOfDay())
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