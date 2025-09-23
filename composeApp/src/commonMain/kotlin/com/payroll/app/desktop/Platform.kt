package com.payroll.app.desktop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform