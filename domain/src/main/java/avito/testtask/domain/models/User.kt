package avito.testtask.domain.models
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarImageUrl: String?,
    val phoneNumber: String?,
)