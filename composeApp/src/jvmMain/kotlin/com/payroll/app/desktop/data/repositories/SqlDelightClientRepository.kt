package com.payroll.app.desktop.data.repositories

import com.payroll.app.desktop.ClientEntity
import com.payroll.app.desktop.database.PayrollDatabase
import com.payroll.app.desktop.domain.models.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SQLDelight implementation of LocalClientRepository
 * Provides local database storage for clients
 */
class SqlDelightClientRepository(
    private val database: PayrollDatabase
) : LocalClientRepository {

    private val queries = database.payrollDatabaseQueries

    override suspend fun getAll(): List<Client> = withContext(Dispatchers.IO) {
        queries.selectAllClients().executeAsList().map { it.toClient() }
    }

    override suspend fun getById(id: Long): Client? = withContext(Dispatchers.IO) {
        queries.selectClientById(id).executeAsOneOrNull()?.toClient()
    }

    override suspend fun getByEmployeeId(employeeId: String): List<Client> = withContext(Dispatchers.IO) {
        queries.selectClientsByEmployeeId(employeeId).executeAsList().map { it.toClient() }
    }

    override suspend fun getByName(name: String): Client? = withContext(Dispatchers.IO) {
        queries.selectClientByName(name).executeAsOneOrNull()?.toClient()
    }

    override suspend fun getByEmployeeIdAndName(employeeId: String, name: String): Client? = withContext(Dispatchers.IO) {
        queries.selectClientByEmployeeIdAndName(employeeId, name).executeAsOneOrNull()?.toClient()
    }

    override suspend fun insert(client: Client): Long = withContext(Dispatchers.IO) {
        queries.insertClient(
            name = client.name,
            price = client.price,
            employee_price = client.employeePrice,
            company_price = client.companyPrice,
            employee_id = client.employeeId,
            pending_payment = if (client.pendingPayment) 1L else 0L
        )
        queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun update(client: Client) = withContext(Dispatchers.IO) {
        queries.updateClient(
            id = client.id,
            name = client.name,
            price = client.price,
            employee_price = client.employeePrice,
            company_price = client.companyPrice,
            employee_id = client.employeeId,
            pending_payment = if (client.pendingPayment) 1L else 0L
        )
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteClient(id)
    }

    override suspend fun deleteByEmployeeId(employeeId: String) = withContext(Dispatchers.IO) {
        queries.deleteClientsByEmployeeId(employeeId)
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        queries.deleteAllClients()
    }

    override suspend fun count(): Long = withContext(Dispatchers.IO) {
        queries.countClients().executeAsOne()
    }

    /**
     * Extension function to convert database row to domain model
     */
    private fun ClientEntity.toClient(): Client {
        return Client(
            id = this.id,
            name = this.name,
            price = this.price,
            employeePrice = this.employee_price,
            companyPrice = this.company_price,
            employeeId = this.employee_id,
            pendingPayment = this.pending_payment == 1L
        )
    }
}
