package avito.testtask.domain.usecases.auth

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.AuthRepository

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun execute(): OperationResult<User?> {
        return authRepository.getCurrentUser()
    }
}