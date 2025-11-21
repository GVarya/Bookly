package avito.testtask.bookly

//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import avito.testtask.bookly.ui.theme.BooklyTheme
//


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import avito.testtask.bookly.ui.theme.BooklyTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BooklyTheme {
                BookApp()
            }
        }
    }
}

@Composable
fun BookApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = "books", Modifier.padding(paddingValues)) {
            composable("login") { LoginScreen(navController) }
            composable("books") { DownloadedBooksScreen(navController) }
            composable("upload") { UploadBookScreen(navController) }
            composable("reader/{bookId}", arguments = listOf(navArgument("bookId") { defaultValue = "" })) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                BookReaderScreen(navController, bookId)
            }
            composable("profile") { ProfileScreen(navController) }
        }
    }
}

// Bottom Navigation Bar
@Composable
fun BottomNavBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountBox, contentDescription = "Мои книги") },
            label = { Text("Мои книги") },
            selected = false,
            onClick = { navController.navigate("books") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Загрузка книги") },
            label = { Text("Загрузка") },
            selected = false,
            onClick = { navController.navigate("upload") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Профиль") },
            label = { Text("Профиль") },
            selected = false,
            onClick = { navController.navigate("profile") }
        )
    }
}

// Экран входа
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вход в аккаунт") },
                actions = {
                    TextButton(onClick = { /* Навигация на регистрацию, пока заглушка */ }) {
                        Text("Регистрация")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) { data ->
                Snackbar { Text(data.visuals.message) }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    // Тут будет логика входа
                    // Для UI блокируем кнопку при загрузке
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Войти")
                }
            }
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}

// Экран загрузки книги
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadBookScreen(navController: NavHostController) {
    var fileName by remember { mutableStateOf<String?>(null) }
    var bookTitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Загрузка книги") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { /* Открыть системный выбор файла */ }) {
                Text("Выбрать файл")
            }
            fileName?.let { Text("Файл: $it") }

            OutlinedTextField(
                value = bookTitle,
                onValueChange = { bookTitle = it },
                label = { Text("Название книги") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Автор") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { /* Запустить загрузку */ },
                enabled = !isUploading && fileName != null && bookTitle.isNotBlank() && author.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Загрузить")
            }

            if (isUploading) {
                LinearProgressIndicator(progress = uploadProgress, modifier = Modifier.fillMaxWidth())
                Text("${(uploadProgress * 100).toInt()} %")
            }

            errorMessage?.let { Text(it, color = Color.Red) }
            successMessage?.let { Text(it, color = Color.Green) }
        }
    }
}

// Экран скачанных книг
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedBooksScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    val books = remember { // Заглушка списка книг
        mutableStateListOf(
            Book("Война и мир", "Лев Толстой", false),
            Book("Преступление и наказание", "Фёдор Достоевский", true),
        )
    }
    val filteredBooks = books.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мои книги") })
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск книг…") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            when {
                books.isEmpty() -> Text("Список книг пуст", modifier = Modifier.align(Alignment.CenterHorizontally))
                filteredBooks.isEmpty() -> Text("Ничего не найдено", modifier = Modifier.align(Alignment.CenterHorizontally))
                else -> LazyColumn {
                    items(filteredBooks) { book ->
                        BookListItem(book = book,
                            onClick = { /* Открыть экран чтения с bookId */ },
                            onIconClick = { /* Удалить или скачать */ })
                    }
                }
            }
        }
    }
}

@Composable
fun BookListItem(book: Book, onClick: () -> Unit, onIconClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Menu, contentDescription = "Обложка", modifier = Modifier.size(48.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(book.title, fontSize = 18.sp)
            Text(book.author, fontSize = 14.sp, color = Color.Gray)
        }
        IconButton(onClick = onIconClick) {
            Icon(
                imageVector = if (book.isDownloaded) Icons.Default.Delete else Icons.Default.Check,
                contentDescription = if (book.isDownloaded) "Удалить" else "Скачать"
            )
        }
    }
}

data class Book(val title: String, val author: String, val isDownloaded: Boolean)

// Экран чтения книги
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(navController: NavHostController, bookId: String) {
    var fontSize by remember { mutableStateOf(16.sp) }
    var themeMode by remember { mutableStateOf("light") }
    var showSettings by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.45f) } // 45% прочитано пример

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чтение книги") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Настройки отображения")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            // Текст книги (заглушка)
            Text(
                text = "Текст книги здесь...\n".repeat(100),
                fontSize = fontSize,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
            if (showSettings) {
                BookReaderSettings(
                    fontSize = fontSize,
                    onFontSizeChange = { fontSize = it },
                    themeMode = themeMode,
                    onThemeChange = { themeMode = it },
                    onClose = { showSettings = false },
                    progress = progress
                )
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            )
        }
    }
}

@Composable
fun BookReaderSettings(
    fontSize: androidx.compose.ui.unit.TextUnit,
    onFontSizeChange: (androidx.compose.ui.unit.TextUnit) -> Unit,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    onClose: () -> Unit,
    progress: Float
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Настройки чтения", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text("Размер шрифта")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(14.sp, 16.sp, 18.sp).forEach { size ->
                    Button(onClick = { onFontSizeChange(size) }) {
                        Text("$size")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Тема")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("light", "dark").forEach { theme ->
                    Button(onClick = { onThemeChange(theme) }) {
                        Text(theme.capitalize())
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}% прочитано")

            Spacer(Modifier.height(8.dp))
            Button(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
                Text("Закрыть")
            }
        }
    }
}

// Экран профиля
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    var userName by remember { mutableStateOf("Имя пользователя") }
    var email by remember { mutableStateOf("user@example.com") }
    var phone by remember { mutableStateOf("+7 123 456 7890") }
    var showEdit by remember { mutableStateOf(false) }
    var userPhotoUri by remember { mutableStateOf<String?>(null) } // Пока заглушка

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userPhotoUri == null) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Аватар",
                    modifier = Modifier.size(100.dp)
                )
            } else {
                // Здесь можно загрузить фото пользователя
            }
            Spacer(Modifier.height(8.dp))

            if (showEdit) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Имя пользователя") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { showEdit = false }) {
                    Text("Сохранить")
                }
            } else {
                Text(userName, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(email)
                Spacer(Modifier.height(4.dp))
                Text(phone)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showEdit = true }) {
                    Text("Редактировать")
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = { /* Логика выхода из аккаунта */ }) {
                Text("Выйти из аккаунта")
            }
        }
    }
}


