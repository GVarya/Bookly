package avito.testtask.domain.usecases.user

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.UserRepository

class UpdateUserUsecase(
    private val userRepository: UserRepository
) {
    suspend fun execute(user: User): OperationResult<User> {
        return userRepository.updateUser(user)
    }
}