package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.domain.models.Client

/**
 * Repository interface for local Client data access
 * Implementation will use SQLDelight for desktop
 */
interface LocalClientRepository {
    suspend fun getAll(): List<Client>
    suspend fun getById(id: Long): Client?
    suspend fun getByEmployeeId(employeeId: String): List<Client>
    suspend fun getByName(name: String): Client?
    suspend fun getByEmployeeIdAndName(employeeId: String, name: String): Client?
    suspend fun insert(client: Client): Long
    suspend fun update(client: Client)
    suspend fun delete(id: Long)
    suspend fun deleteByEmployeeId(employeeId: String)
    suspend fun deleteAll()
    suspend fun count(): Long
}
