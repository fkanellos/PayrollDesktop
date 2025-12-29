package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.domain.models.Employee

/**
 * Repository interface for local Employee data access
 * Implementation will use SQLDelight for desktop
 */
interface LocalEmployeeRepository {
    suspend fun getAll(): List<Employee>
    suspend fun getById(id: String): Employee?
    suspend fun insert(employee: Employee)
    suspend fun update(employee: Employee)
    suspend fun delete(id: String)
    suspend fun deleteAll()
    suspend fun count(): Long
}
