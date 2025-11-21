package avito.testtask.domain.repos

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User

interface AuthRepository {
    suspend fun login(email: String, password: String): OperationResult<User>
    suspend fun register(name: String, email: String, password: String): OperationResult<User>
    suspend fun logout(): OperationResult<Unit>
    suspend fun getCurrentUser(): OperationResult<User?>
}