package avito.testtask.data.repos_implementations

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val firebaseAuth: FirebaseAuth) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): OperationResult<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                OperationResult.Success(firebaseUser.toUser())
            } else {
                OperationResult.Error("Login failed")
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Login failed")
        }    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): OperationResult<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                OperationResult.Success(firebaseUser.toUser())
            } else {
                OperationResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun logout(): OperationResult<Unit> {
        return try {
            firebaseAuth.signOut()
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Logout failed")
        }
    }

    override suspend fun getCurrentUser(): OperationResult<User?> {
        return try {
            val currentUser = firebaseAuth.currentUser
            OperationResult.Success(currentUser?.toUser())
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to get current user")
        }
    }
}
fun FirebaseUser.toUser(): User {
    return User(
        id = uid,
        name = displayName ?: "",
        email = email ?: "",
        avatarImageUrl = photoUrl?.toString(),
        phoneNumber = phoneNumber
    )
}