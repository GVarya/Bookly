package avito.testtask.bookly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import avito.testtask.bookly.viewmodels.BooksViewModel
import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    navController: NavController,
    booksViewModel: BooksViewModel = koinViewModel()
) {
    val booksState by booksViewModel.booksState.collectAsState()
    val searchQuery by booksViewModel.searchQuery.collectAsState()
    val isLoading by booksViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мои книги") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SearchTextField(
                value = searchQuery,
                onValueChange = { booksViewModel.searchBooks(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when (booksState) {
                is OperationResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is OperationResult.Error -> {
                    ErrorState(
                        message = (booksState as OperationResult.Error).message,
                        onRetry = { booksViewModel.loadBooks() }
                    )
                }

                is OperationResult.Success -> {
                    val books = (booksState as OperationResult.Success).data
                    if (books.isEmpty()) {
                        EmptyState()
                    } else {
                        BooksList(
                            books = books,
                            onBookClick = { bookId ->
                                navController.navigate("reader/$bookId")
                            },
                            onActionClick = { book, action ->
                                when (action) {
                                    BookAction.DOWNLOAD -> booksViewModel.downloadBook(book)
                                    BookAction.DELETE -> booksViewModel.deleteBook(book)
                                }
                            },
                            isLoading = isLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Поиск книг...") },
        modifier = modifier,
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Поиск")
        },
        singleLine = true
    )
}

@Composable
fun BooksList(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    onActionClick: (Book, BookAction) -> Unit,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(books) { book ->
            BookListItem(
                book = book,
                onBookClick = { onBookClick(book.id) },
                onActionClick = { action -> onActionClick(book, action) },
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun BookListItem(
    book: Book,
    onBookClick: () -> Unit,
    onActionClick: (BookAction) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !isLoading,
                    onClick = onBookClick
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = "Обложка",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = {
                    val action = if (book.downloaded) BookAction.DELETE else BookAction.DOWNLOAD
                    onActionClick(action)
                },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = if (book.downloaded) Icons.Default.Delete else Icons.Default.Download,
                    contentDescription = if (book.downloaded) "Удалить" else "Скачать",
                    tint = if (book.downloaded) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = "Нет книг",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "У вас пока нет скачанных книг",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

enum class BookAction {
    DOWNLOAD, DELETE
}