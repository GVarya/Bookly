package avito.testtask.data

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): OperationResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): OperationResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): OperationResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUser(): OperationResult<User?> {
        TODO("Not yet implemented")
    }
}