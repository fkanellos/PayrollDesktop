package com.payroll.app.desktop.core.di

import com.payroll.app.desktop.data.repositories.ClientRepository
import com.payroll.app.desktop.data.repositories.EmployeeRepository
import com.payroll.app.desktop.data.repositories.PayrollRepository
import com.payroll.app.desktop.domain.service.DatabaseSyncService
import com.payroll.app.desktop.presentation.client.ClientManagementViewModel
import com.payroll.app.desktop.presentation.employee.EmployeeManagementViewModel
import com.payroll.app.desktop.presentation.payroll.PayrollViewModel
import org.koin.dsl.module

/**
 * Common Koin DI module for shared dependencies
 * Repositories are now expect/actual and configured in platform modules
 * ViewModels are defined in ViewModelModule
 */
val commonModule = module {
    // Empty - all dependencies are platform-specific (localModule) or in ViewModelModule
}