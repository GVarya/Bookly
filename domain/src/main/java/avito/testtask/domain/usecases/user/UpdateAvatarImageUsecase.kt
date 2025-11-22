package avito.testtask.domain.usecases.user

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.UserRepository

class UpdateAvatarImageUsecase(
    private val userRepository: UserRepository
) {
    suspend fun execute(imageUri: String): OperationResult<String> {
        return userRepository.updateAvatarImage(imageUri)
    }
}