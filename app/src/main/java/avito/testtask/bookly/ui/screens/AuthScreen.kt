package avito.testtask.bookly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import avito.testtask.bookly.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    LaunchedEffect(authState) {
        if (authState is avito.testtask.domain.models.AuthState.Authenticated) {
            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    LaunchedEffect(isLoginMode) {
        authViewModel.clearError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isLoginMode) "Вход в аккаунт"
                        else "Регистрация"
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = "Логотип",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Имя"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    isError = authState is avito.testtask.domain.models.AuthState.Error,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = authState is avito.testtask.domain.models.AuthState.Error,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Пароль"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = authState is avito.testtask.domain.models.AuthState.Error,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoginMode) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтвердите пароль") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Подтверждение пароля"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = authState is avito.testtask.domain.models.AuthState.Error,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (authState is avito.testtask.domain.models.AuthState.Error) {
                Text(
                    text = (authState as avito.testtask.domain.models.AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    when {
                        isLoginMode -> {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                authViewModel.login(email, password)
                            }
                        }
                        else -> {
                            if (name.isNotEmpty() && email.isNotEmpty() &&
                                password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                if (password == confirmPassword) {
                                    authViewModel.register(name, email, password)
                                } else {
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && when {
                    isLoginMode -> email.isNotEmpty() && password.isNotEmpty()
                    else -> name.isNotEmpty() && email.isNotEmpty() &&
                            password.isNotEmpty() && confirmPassword.isNotEmpty() &&
                            password == confirmPassword
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isLoginMode) "Войти" else "Зарегистрироваться")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    if (isLoginMode) {
                        name = ""
                        confirmPassword = ""
                    }
                },
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoginMode)
                        "Нет аккаунта? Зарегистрируйтесь"
                    else
                        "Уже есть аккаунт? Войдите",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}