package com.payroll.app.desktop.di

import com.payroll.app.desktop.data.repositories.CalendarRepository
import com.payroll.app.desktop.data.repositories.LocalClientRepository
import com.payroll.app.desktop.data.repositories.LocalEmployeeRepository
import com.payroll.app.desktop.data.repositories.SqlDelightClientRepository
import com.payroll.app.desktop.data.repositories.SqlDelightEmployeeRepository
import com.payroll.app.desktop.database.DriverFactory
import com.payroll.app.desktop.database.PayrollDatabase
import com.payroll.app.desktop.domain.service.ClientMatchingService
import com.payroll.app.desktop.domain.service.ClientPersistenceService
import com.payroll.app.desktop.domain.service.PayrollCalculationService
import com.payroll.app.desktop.google.GoogleCalendarRepository
import com.payroll.app.desktop.google.GoogleCredentialProvider
import com.payroll.app.desktop.google.GoogleSheetsService
import com.payroll.app.desktop.service.DesktopClientPersistenceService
import org.koin.dsl.module

/**
 * Local Koin DI module for desktop-specific dependencies
 * Provides SQLDelight database and Google Calendar/Sheets integration
 *
 * STANDALONE DESKTOP APP ARCHITECTURE:
 * - SQLite for local storage (via SQLDelight)
 * - Google Calendar API for reading events (READ ONLY!)
 * - Google Sheets API for writing clients/reports (WRITE allowed)
 * - NO backend server calls!
 *
 * IMPORTANT:
 * - Google Calendar = READ ONLY (never write to calendar!)
 * - Google Sheets = WRITE allowed (for adding new clients to correct employee tab)
 */
val localModule = module {

    // Database
    single { DriverFactory.createDriver() }
    single { PayrollDatabase(get()) }

    // Local Repositories (SQLDelight)
    single<LocalEmployeeRepository> { SqlDelightEmployeeRepository(get()) }
    single<LocalClientRepository> { SqlDelightClientRepository(get()) }

    // Google APIs
    single { GoogleCredentialProvider() }
    single<CalendarRepository> { GoogleCalendarRepository(get()) }  // READ ONLY!
    single { GoogleSheetsService(get()) }  // WRITE allowed

    // Services
    single { ClientMatchingService() }
    single { PayrollCalculationService(get()) }

    // Client Persistence Service (SQLite + Google Sheets)
    // This is the main service for adding clients in standalone mode
    single<ClientPersistenceService> {
        DesktopClientPersistenceService(
            localClientRepository = get(),
            googleSheetsService = get()
        )
    }
}
