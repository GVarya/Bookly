package avito.testtask.domain.usecases.auth

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.AuthRepository

class LogOutUsecase(
    private val authRepository: AuthRepository
) {
    suspend fun execute(): OperationResult<Unit> {
        return authRepository.logout()
    }
}