package avito.testtask.data.repos_implementations

import android.net.Uri
import androidx.core.net.toUri
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.repos.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    private val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    override suspend fun getUser(): OperationResult<User> {
        return try {
            val user = currentUser
            if (user != null) {
                OperationResult.Success(user.toUser())
            } else {
                OperationResult.Error("User not found")
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to get user")
        }    }

    override suspend fun updateUser(user: User): OperationResult<User> {
        return try {
            val currentUser = currentUser
            if (currentUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(user.name)
                    .setPhotoUri(user.avatarImageUrl?.toUri())
                    .build()

                currentUser.updateProfile(profileUpdates).await()

                val userData = mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "avatarImageUrl" to user.avatarImageUrl,
                    "phoneNumber" to user.phoneNumber
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(userData, SetOptions.merge())
                    .await()

                OperationResult.Success(user)
            } else {
                OperationResult.Error("User not found")
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to update user")
        }
    }

    override suspend fun updateAvatarImage(imageUri: String): OperationResult<String> {
        return try {
            val currentUser = currentUser
            if (currentUser != null) {
                val storageRef = storage.reference.child("avatars/${currentUser.uid}")
                val uploadTask = storageRef.putFile(imageUri.toUri()).await()
                val downloadUrl = storageRef.downloadUrl.await()

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUrl)
                    .build()

                currentUser.updateProfile(profileUpdates).await()

                OperationResult.Success(downloadUrl.toString())
            } else {
                OperationResult.Error("User not found")
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to update avatar")
        }
    }
}