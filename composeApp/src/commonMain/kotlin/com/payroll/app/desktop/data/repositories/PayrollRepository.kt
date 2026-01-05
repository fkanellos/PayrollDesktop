package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.*

/**
 * Repository for payroll operations
 * Now uses local services instead of backend API
 */
expect class PayrollRepository {
    suspend fun getAllEmployees(): RepositoryResult<List<Employee>>
    suspend fun getEmployee(id: String): RepositoryResult<Employee>
    suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>>
    suspend fun calculatePayroll(request: PayrollRequest): RepositoryResult<PayrollResponse>
    suspend fun createClient(client: Client): RepositoryResult<Client>
}

/**
 * Employee Repository for CRUD operations
 * Now uses local database instead of backend API
 */
expect class EmployeeRepository {
    suspend fun getAll(): RepositoryResult<List<Employee>>
    suspend fun getById(id: String): RepositoryResult<Employee>
    suspend fun createEmployee(employee: Employee): RepositoryResult<Employee>
    suspend fun updateEmployee(employee: Employee): RepositoryResult<Employee>
    suspend fun deleteEmployee(id: String): RepositoryResult<Boolean>
    suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>>
}