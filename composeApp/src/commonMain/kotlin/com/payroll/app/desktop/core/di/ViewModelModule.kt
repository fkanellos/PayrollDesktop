package com.payroll.app.desktop.core.di

import com.payroll.app.desktop.presentation.client.ClientManagementViewModel
import com.payroll.app.desktop.presentation.employee.EmployeeManagementViewModel
import com.payroll.app.desktop.presentation.payroll.PayrollViewModel
import com.payroll.app.desktop.presentation.settings.SettingsViewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels
 *
 * All ViewModels should be defined here with their dependencies.
 * UI layer should NOT directly instantiate ViewModels or inject repositories.
 */
val viewModelModule = module {

    // PayrollViewModel - depends on UseCases and services
    factory {
        PayrollViewModel(
            payrollRepository = get(),
            databaseSyncService = get(),
            payrollCalculationUseCase = get(),
            matchConfirmationUseCase = get(),
            clientQuickAddUseCase = get(),
            payrollExportUseCase = get()
        )
    }

    // EmployeeManagementViewModel - depends on EmployeeRepository
    factory {
        EmployeeManagementViewModel(
            employeeRepository = get()
        )
    }

    // ClientManagementViewModel - depends on PayrollRepository and ClientRepository
    factory {
        ClientManagementViewModel(
            payrollRepository = get(),
            clientRepository = get()
        )
    }

    // SettingsViewModel - depends on DatabaseSyncService
    factory {
        SettingsViewModel(
            databaseSyncService = get()
        )
    }
}
