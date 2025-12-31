package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.EmployeeEntity
import com.payroll.app.desktop.database.PayrollDatabase
import com.payroll.app.desktop.domain.models.Employee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SQLDelight implementation of LocalEmployeeRepository
 * Provides local database storage for employees
 */
class SqlDelightEmployeeRepository(
    private val database: PayrollDatabase
) : LocalEmployeeRepository {

    private val queries = database.payrollDatabaseQueries

    override suspend fun getAll(): List<Employee> = withContext(Dispatchers.IO) {
        queries.selectAllEmployees().executeAsList().map { it.toEmployee() }
    }

    override suspend fun getById(id: String): Employee? = withContext(Dispatchers.IO) {
        queries.selectEmployeeById(id).executeAsOneOrNull()?.toEmployee()
    }

    override suspend fun insert(employee: Employee) = withContext(Dispatchers.IO) {
        queries.insertEmployee(
            id = employee.id,
            name = employee.name,
            email = employee.email,
            calendar_id = employee.calendarId,
            color = employee.color,
            sheet_name = employee.sheetName,
            supervision_price = employee.supervisionPrice
        )
    }

    override suspend fun update(employee: Employee) = withContext(Dispatchers.IO) {
        queries.updateEmployee(
            id = employee.id,
            name = employee.name,
            email = employee.email,
            calendar_id = employee.calendarId,
            color = employee.color,
            sheet_name = employee.sheetName,
            supervision_price = employee.supervisionPrice
        )
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        queries.deleteEmployee(id)
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        queries.deleteAllEmployees()
    }

    override suspend fun count(): Long = withContext(Dispatchers.IO) {
        queries.countEmployees().executeAsOne()
    }

    /**
     * Extension function to convert database row to domain model
     */
    private fun EmployeeEntity.toEmployee(): Employee {
        return Employee(
            id = this.id,
            name = this.name,
            email = this.email,
            calendarId = this.calendar_id,
            color = this.color,
            sheetName = this.sheet_name,
            supervisionPrice = this.supervision_price
        )
    }
}
