package avito.testtask.bookly.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import avito.testtask.bookly.viewmodels.ReadingViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import avito.testtask.domain.models.OperationResult
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreeen(
    navController: NavController,
    bookId: String,
    readingViewModel: ReadingViewModel = koinViewModel()
) {
    var fontSize by remember { mutableStateOf(16.sp) }
    var isSettingsVisible by remember { mutableStateOf(false) }
    var currentTheme by remember { mutableStateOf(ReadingTheme.Light) }

    val bookContent by readingViewModel.bookContent.collectAsState()
    val readingProgress by readingViewModel.readingProgress.collectAsState()
    val currentBook by readingViewModel.currentBook.collectAsState()
    val isLoading by readingViewModel.isLoading.collectAsState()

    LaunchedEffect(bookId) {
        currentBook?.let { book ->
            if (book.id == bookId) return@LaunchedEffect
        }
        readingViewModel.loadBookById(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentBook?.title ?: "Чтение книги",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { isSettingsVisible = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                bookContent is avito.testtask.domain.models.OperationResult.Success -> {
                    val content = (bookContent as avito.testtask.domain.models.OperationResult.Success).data

                    Text(
                        text = content,
                        fontSize = fontSize,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        color = when (currentTheme) {
                            ReadingTheme.Light -> MaterialTheme.colorScheme.onSurface
                            ReadingTheme.Dark -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    readingProgress?.let { progress ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = progress.progress,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${(progress.progress * 100).toInt()}% прочитано",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                bookContent is avito.testtask.domain.models.OperationResult.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ошибка загрузки книги",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                currentBook?.let {
                                    readingViewModel.loadBookContent(it)
                                    }
                            }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
            }

            if (isSettingsVisible) {
                ReadingSettingsModal(
                    fontSize = fontSize,
                    onFontSizeChange = { fontSize = it },
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    onDismiss = { isSettingsVisible = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSettingsModal(
    fontSize: androidx.compose.ui.unit.TextUnit,
    onFontSizeChange: (androidx.compose.ui.unit.TextUnit) -> Unit,
    currentTheme: ReadingTheme,
    onThemeChange: (ReadingTheme) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Настройки чтения",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Размер шрифта",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(14.sp, 16.sp, 18.sp, 20.sp).forEach { size ->
                    Text(
                        text = "Аа",
                        fontSize = size,
                        modifier = Modifier
                            .clickable { onFontSizeChange(size) }
                            .padding(12.dp)
                            .background(
                                if (fontSize == size) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.small
                            )
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Тема",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReadingTheme.entries.forEach { theme ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onThemeChange(theme) }
                            .background(
                                color = when (theme) {
                                    ReadingTheme.Light -> MaterialTheme.colorScheme.surface
                                    ReadingTheme.Dark -> MaterialTheme.colorScheme.surface
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = if (currentTheme == theme) 2.dp else 1.dp,
                                color = if (currentTheme == theme) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (theme) {
                                ReadingTheme.Light -> "С"
                                ReadingTheme.Dark -> "Т"
                            },
                            color = when (theme) {
                                ReadingTheme.Light -> MaterialTheme.colorScheme.onSurface
                                ReadingTheme.Dark -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Готово")
            }
        }
    }
}

enum class ReadingTheme {
    Light, Dark
}