package avito.testtask.domain.usecases.auth

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun execute(name: String, email: String, password: String): OperationResult<User> {
        return authRepository.register(name, email, password)
    }
}