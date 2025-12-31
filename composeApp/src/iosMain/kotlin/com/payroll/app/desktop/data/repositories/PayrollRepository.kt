package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.*

/**
 * iOS stub for PayrollRepository
 */
actual class PayrollRepository {
    actual suspend fun getAllEmployees(): RepositoryResult<List<Employee>> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun getEmployee(id: String): RepositoryResult<Employee> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun calculatePayroll(request: PayrollRequest): RepositoryResult<PayrollResponse> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun createClient(client: Client): RepositoryResult<Client> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }
}

/**
 * iOS stub for EmployeeRepository
 */
actual class EmployeeRepository {
    actual suspend fun getAll(): RepositoryResult<List<Employee>> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun getById(id: String): RepositoryResult<Employee> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun createEmployee(employee: Employee): RepositoryResult<Employee> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun updateEmployee(employee: Employee): RepositoryResult<Employee> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun deleteEmployee(id: String): RepositoryResult<Boolean> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }
}