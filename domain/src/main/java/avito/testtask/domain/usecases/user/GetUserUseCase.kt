package avito.testtask.domain.usecases.user

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.UserRepository

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend fun execute(): OperationResult<User> {
        return userRepository.getUser()
    }
}