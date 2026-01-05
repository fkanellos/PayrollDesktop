package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.Client

/**
 * Repository for Client CRUD operations
 * Now uses local database instead of backend API
 */
expect class ClientRepository {
    suspend fun getByEmployeeId(employeeId: String): RepositoryResult<List<Client>>
    suspend fun getAll(): RepositoryResult<List<Client>>
    suspend fun createClient(client: Client): RepositoryResult<Client>
    suspend fun updateClient(client: Client): RepositoryResult<Client>
    suspend fun deleteClient(id: Long): RepositoryResult<Boolean>
}