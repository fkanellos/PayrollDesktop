package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.network.PayrollApiService
import com.payroll.app.desktop.domain.models.*

/**
 * Repository for payroll operations
 */
class PayrollRepository(
    private val apiService: PayrollApiService
) {
    /**
     * Get all employees
     */
    suspend fun getAllEmployees(): RepositoryResult<List<Employee>> {
        return apiService.getEmployees()
    }

    /**
     * Get a single employee by ID
     */
    suspend fun getEmployee(id: String): RepositoryResult<Employee> {
        return apiService.getEmployee(id)
    }

    /**
     * Get all clients for a specific employee
     */
    suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return apiService.getEmployeeClients(employeeId)
    }

    /**
     * Calculate payroll for a given request
     */
    suspend fun calculatePayroll(request: PayrollRequest): RepositoryResult<PayrollResponse> {
        return apiService.calculatePayroll(request)
    }

    /**
     * Get calendar events for an employee within a date range
     */
    suspend fun getCalendarEvents(
        employeeId: String,
        startDate: String,
        endDate: String
    ): RepositoryResult<Map<String, Any>> {
        return apiService.getCalendarEvents(employeeId, startDate, endDate)
    }

    /**
     * Test backend connection
     */
    suspend fun testBackendConnection(): RepositoryResult<String> {
        return apiService.testConnection()
    }

    /**
     * Download PDF for a payroll calculation
     */
    suspend fun downloadPdf(payrollId: String): RepositoryResult<ByteArray> {
        return apiService.downloadPdf(payrollId)
    }

    /**
     * Check if payroll exists in Google Sheets
     */
    suspend fun checkPayrollInSheets(payrollId: String): RepositoryResult<CheckSheetsResponse> {
        return apiService.checkPayrollInSheets(payrollId)
    }

    /**
     * Sync payroll to Google Sheets
     */
    suspend fun syncPayrollToSheets(payrollId: String): RepositoryResult<SyncSheetsResponse> {
        return apiService.syncPayrollToSheets(payrollId)
    }

    /**
     * Sync database from Google Sheets
     */
    suspend fun syncDatabase(): RepositoryResult<SyncDatabaseResponse> {
        return apiService.syncDatabase()
    }

    /**
     * Get database statistics
     */
    suspend fun getDatabaseStats(): RepositoryResult<DatabaseStatsResponse> {
        return apiService.getDatabaseStats()
    }

    /**
     * Create a new client
     */
    suspend fun createClient(client: Client): RepositoryResult<Client> {
        return apiService.createClient(client)
    }
}

/**
 * Employee Repository for CRUD operations
 */
class EmployeeRepository(
    private val apiService: PayrollApiService
) {
    /**
     * Get all employees
     */
    suspend fun getAll(): RepositoryResult<List<Employee>> {
        return apiService.getEmployees()
    }

    /**
     * Get a single employee by ID
     */
    suspend fun getById(id: String): RepositoryResult<Employee> {
        return apiService.getEmployee(id)
    }

    /**
     * Create a new employee
     */
    suspend fun createEmployee(employee: Employee): RepositoryResult<Employee> {
        return apiService.createEmployee(employee)
    }

    /**
     * Update an existing employee
     */
    suspend fun updateEmployee(employee: Employee): RepositoryResult<Employee> {
        return apiService.updateEmployee(employee.id, employee)
    }

    /**
     * Delete an employee by ID
     */
    suspend fun deleteEmployee(id: String): RepositoryResult<Boolean> {
        return apiService.deleteEmployee(id)
    }

    /**
     * Get all clients for a specific employee
     */
    suspend fun getEmployeeClients(employeeId: String): RepositoryResult<List<Client>> {
        return apiService.getEmployeeClients(employeeId)
    }
}