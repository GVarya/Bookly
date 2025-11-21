package avito.testtask.domain.usecases.auth

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.AuthRepository

class LoginUsecase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): OperationResult<User> {
        return authRepository.login(email, password)
    }
}