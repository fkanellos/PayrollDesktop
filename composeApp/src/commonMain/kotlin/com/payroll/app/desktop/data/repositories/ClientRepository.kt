package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.core.network.PayrollApiService
import com.payroll.app.desktop.domain.models.Client

/**
 * Repository for Client CRUD operations
 * Implements full CRUD via REST API
 */
class ClientRepository(
    private val apiService: PayrollApiService
) {
    /**
     * Get all clients for a specific employee
     */
    suspend fun getByEmployeeId(employeeId: String): RepositoryResult<List<Client>> {
        return apiService.getEmployeeClients(employeeId)
    }

    /**
     * Get all clients
     */
    suspend fun getAll(): RepositoryResult<List<Client>> {
        return apiService.getAllClients()
    }

    /**
     * Create a new client
     */
    suspend fun createClient(client: Client): RepositoryResult<Client> {
        return apiService.createClient(client)
    }

    /**
     * Update an existing client
     */
    suspend fun updateClient(client: Client): RepositoryResult<Client> {
        return apiService.updateClient(client.id, client)
    }

    /**
     * Delete a client by ID
     */
    suspend fun deleteClient(id: Long): RepositoryResult<Boolean> {
        return apiService.deleteClient(id)
    }
}
