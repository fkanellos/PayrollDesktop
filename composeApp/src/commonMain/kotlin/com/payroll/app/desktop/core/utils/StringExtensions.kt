package com.payroll.app.desktop.core.utils

/**
 * Simple string formatting extension for KMP
 * Replaces %s placeholders with arguments in order
 */
fun String.format(vararg args: Any?): String {
    var result = this
    args.forEach { arg ->
        result = result.replaceFirst("%s", arg.toString())
    }
    return result
}
