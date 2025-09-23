package com.payroll.app.desktop.utils

fun Double.toEuroString(): String {
    val rounded = (this * 100).toInt() / 100.0
    return "€$rounded"
}