package avito.testtask.bookly.ui.screens

import androidx.compose.material3.LinearProgressIndicator
import avito.testtask.bookly.viewmodels.UploadState

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import avito.testtask.bookly.viewmodels.BooksViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadBookScreen(
    navController: NavController,
    booksViewModel: BooksViewModel = koinViewModel()
) {
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    val uploadState by booksViewModel.uploadState.collectAsState()
    val isLoading by booksViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Загрузка книги") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // TODO: Открыть системный выбор файла
                    selectedFileName = "example_book.pdf"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Выбрать файл")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выбрать файл")
            }

            selectedFileName?.let { fileName ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Выбранный файл: $fileName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название книги") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Автор") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (uploadState) {
                is UploadState.Loading -> {
                    val progress = (uploadState as UploadState.Loading).progress
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = ProgressIndicatorDefaults.linearColor,
                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Загрузка: ${(progress * 100).toInt()}%")
                    }
                }

                is UploadState.Success -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Книга успешно загружена",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        booksViewModel.resetUploadState()
                    }
                }

                is UploadState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ошибка загрузки: ${(uploadState as UploadState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { booksViewModel.resetUploadState() }) {
                            Text("Повторить")
                        }
                    }
                }

                else -> {
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (selectedFileName != null && title.isNotEmpty() && author.isNotEmpty()) {
                        booksViewModel.uploadBook(
                            fileUri = "file_uri_placeholder", // TODO: Заменить на реальный URI
                            title = title,
                            author = author
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && selectedFileName != null &&
                        title.isNotEmpty() && author.isNotEmpty() &&
                        uploadState !is UploadState.Success
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Загрузить книгу")
                }
            }
        }
    }
}