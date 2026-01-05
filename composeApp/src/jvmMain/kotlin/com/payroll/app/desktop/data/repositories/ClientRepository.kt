package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.Client
import com.payroll.app.desktop.google.GoogleSheetsService

/**
 * JVM implementation of ClientRepository using local database and Google Sheets
 */
actual class ClientRepository(
    private val localClientRepo: LocalClientRepository,
    private val googleSheetsService: GoogleSheetsService
) {
    actual suspend fun getByEmployeeId(employeeId: String): RepositoryResult<List<Client>> {
        return try {
            val clients = localClientRepo.getByEmployeeId(employeeId)
            RepositoryResult.Success(clients)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun getAll(): RepositoryResult<List<Client>> {
        return try {
            val clients = localClientRepo.getAll()
            RepositoryResult.Success(clients)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun createClient(client: Client): RepositoryResult<Client> {
        return try {
            val clientId = localClientRepo.insert(client)
            val createdClient = client.copy(id = clientId)
            RepositoryResult.Success(createdClient)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun updateClient(client: Client): RepositoryResult<Client> {
        return try {
            localClientRepo.update(client)
            RepositoryResult.Success(client)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    actual suspend fun deleteClient(id: Long): RepositoryResult<Boolean> {
        return try {
            localClientRepo.delete(id)
            RepositoryResult.Success(true)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }
}
