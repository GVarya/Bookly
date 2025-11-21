package avito.testtask.data

import android.net.Uri
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.UserRepository

class UserRepositoryImpl : UserRepository {
    override suspend fun getUser(): OperationResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(user: User): OperationResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun updateAvatarImage(imageUri: Uri): OperationResult<String> {
        TODO("Not yet implemented")
    }
}