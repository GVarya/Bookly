package avito.testtask.domain.usecases.user

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.UserRepository
import com.sun.jndi.toolkit.url.Uri

class UpdateAvatarImageUsecase(
    private val userRepository: UserRepository
) {
    suspend fun execute(imageUri: Uri): OperationResult<String> {
        return userRepository.updateAvatarImage(imageUri)
    }
}