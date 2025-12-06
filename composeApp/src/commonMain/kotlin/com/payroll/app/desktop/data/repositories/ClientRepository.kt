package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.BaseRepository
import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.network.PayrollApiService
import com.payroll.app.desktop.domain.models.Client

/**
 * Repository for Client CRUD operations
 * Implements full CRUD via REST API
 */
class ClientRepository(
    private val apiService: PayrollApiService
) : BaseRepository<Client, Long> {

    override suspend fun getAll(): Result<List<Client>> {
        return when (val result = apiService.getAllClients()) {
            is RepositoryResult.Success -> Result.success(result.data)
            is RepositoryResult.Error -> Result.failure(result.exception)
        }
    }

    override suspend fun getById(id: Long): Result<Client?> {
        // Note: Backend doesn't have a single client endpoint,
        // we'd need to fetch all and filter or add endpoint
        return Result.failure(Exception("Get single client not implemented - use getByEmployeeId"))
    }

    override suspend fun create(item: Client): Result<Client> {
        return when (val result = apiService.createClient(item)) {
            is RepositoryResult.Success -> Result.success(result.data)
            is RepositoryResult.Error -> Result.failure(result.exception)
        }
    }

    override suspend fun update(id: Long, item: Client): Result<Client> {
        return when (val result = apiService.updateClient(id, item)) {
            is RepositoryResult.Success -> Result.success(result.data)
            is RepositoryResult.Error -> Result.failure(result.exception)
        }
    }

    override suspend fun delete(id: Long): Result<Boolean> {
        return when (val result = apiService.deleteClient(id)) {
            is RepositoryResult.Success -> Result.success(result.data)
            is RepositoryResult.Error -> Result.failure(result.exception)
        }
    }

    /**
     * Get all clients for a specific employee
     */
    suspend fun getByEmployeeId(employeeId: String): RepositoryResult<List<Client>> {
        return apiService.getEmployeeClients(employeeId)
    }

    /**
     * Create client and return RepositoryResult
     */
    suspend fun createClient(client: Client): RepositoryResult<Client> {
        return apiService.createClient(client)
    }

    /**
     * Update client and return RepositoryResult
     */
    suspend fun updateClient(client: Client): RepositoryResult<Client> {
        return apiService.updateClient(client.id, client)
    }

    /**
     * Delete client and return RepositoryResult
     */
    suspend fun deleteClient(id: Long): RepositoryResult<Boolean> {
        return apiService.deleteClient(id)
    }
}
