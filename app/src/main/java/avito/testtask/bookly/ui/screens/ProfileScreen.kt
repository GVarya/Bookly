package avito.testtask.bookly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import avito.testtask.bookly.viewmodels.ProfileViewModel


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import avito.testtask.bookly.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val userState by profileViewModel.userState.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isEditing by profileViewModel.isEditing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Профиль") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (userState) {
                is avito.testtask.domain.models.OperationResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is avito.testtask.domain.models.OperationResult.Success -> {
                    val user = (userState as avito.testtask.domain.models.OperationResult.Success).data
                    ProfileContent(
                        user = user,
                        isEditing = isEditing,
                        isLoading = isLoading,
                        onEditClick = { profileViewModel.setEditing(true) },
                        onSaveClick = { updatedUser ->
                            profileViewModel.updateUser(updatedUser)
                        },
                        onAvatarClick = { imageUri ->
                            profileViewModel.updateAvatar(imageUri)
                        },
                        onLogoutClick = {
                            profileViewModel.clearState()
                            authViewModel.logout()
                            // TODO: Вызвать logout и навигацию на логин
                        }
                    )
                }

                is avito.testtask.domain.models.OperationResult.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ошибка загрузки профиля",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { profileViewModel.loadUser() }) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                else -> {
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: avito.testtask.domain.models.User,
    isEditing: Boolean,
    isLoading: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: (avito.testtask.domain.models.User) -> Unit,
    onAvatarClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    var editedName by remember { mutableStateOf(user.name) }
    var editedEmail by remember { mutableStateOf(user.email) }
    var editedPhone by remember { mutableStateOf(user.phoneNumber ?: "") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clickable(
                    enabled = isEditing && !isLoading,
                    onClick = { /* TODO: Открыть выбор изображения */ }
                )
        ) {
            if (user.avatarImageUrl != null) {
                // TODO: Загрузка изображения
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Аватар",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Имя пользователя") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedEmail,
                        onValueChange = { editedEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ProfileField("Имя пользователя", user.name)
                    ProfileField("Email", user.email)
                    user.phoneNumber?.let { phone ->
                        ProfileField("Телефон", phone)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditing) {
            Button(
                onClick = {
                    val updatedUser = user.copy(
                        name = editedName,
                        phoneNumber = editedPhone.ifEmpty { null }
                    )
                    onSaveClick(updatedUser)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Сохранить")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { /* TODO: Отменить редактирование */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отменить")
            }
        } else {
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Редактировать профиль")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Выход")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти из аккаунта")
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}