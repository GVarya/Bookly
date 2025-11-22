package avito.testtask.domain.repos

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User

interface UserRepository {
    suspend fun getUser(): OperationResult<User>
    suspend fun updateUser(user: User): OperationResult<User>
    suspend fun updateAvatarImage(imageUri: String): OperationResult<String>
}