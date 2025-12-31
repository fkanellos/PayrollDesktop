package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.core.base.RepositoryResult
import com.payroll.app.desktop.domain.models.Client

/**
 * iOS stub for ClientRepository
 */
actual class ClientRepository {
    actual suspend fun getByEmployeeId(employeeId: String): RepositoryResult<List<Client>> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun getAll(): RepositoryResult<List<Client>> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun createClient(client: Client): RepositoryResult<Client> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun updateClient(client: Client): RepositoryResult<Client> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }

    actual suspend fun deleteClient(id: Long): RepositoryResult<Boolean> {
        return RepositoryResult.Error(Exception("Not supported on iOS"))
    }
}