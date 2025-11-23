package avito.testtask.bookly.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.usecases.books.DeleteBookUseCase
import avito.testtask.domain.usecases.books.DownloadBookUseCase
import avito.testtask.domain.usecases.books.GetAllBooksUseCase
import avito.testtask.domain.usecases.books.SearchBookUseCase
import avito.testtask.domain.usecases.books.UploadBookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BooksViewModel(
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val downloadBookUseCase: DownloadBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val searchBookUseCase: SearchBookUseCase,
    private val uploadBookUseCase: UploadBookUseCase
) : ViewModel() {

    private val _booksState = MutableStateFlow<OperationResult<List<Book>>>(OperationResult.Loading)
    val booksState: StateFlow<OperationResult<List<Book>>> = _booksState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _booksState.value = getAllBooksUseCase.execute()
            _isLoading.value = false
        }
    }

    fun downloadBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = downloadBookUseCase.execute(book)) {
                is OperationResult.Success -> {
                    loadBooks()
                }
                is OperationResult.Error -> { // В ui обрабатывается
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = deleteBookUseCase.execute(book)) {
                is OperationResult.Success -> {
                    loadBooks()
                }
                is OperationResult.Error -> { // В ui обрабатывается
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            if (query.isBlank()) {
                loadBooks()
            } else {
                _isLoading.value = true
                _booksState.value = searchBookUseCase.execute(query)
                _isLoading.value = false
            }
        }
    }

    fun uploadBook(fileUri: String, title: String, author: String) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading(0f)
            _isLoading.value = true

            for (progress in 0..100 step 10) {
                _uploadState.value = UploadState.Loading(progress / 100f)
                kotlinx.coroutines.delay(200)
            }

            when (val result = uploadBookUseCase.execute(fileUri, title, author)) {
                is OperationResult.Success -> {
                    _uploadState.value = UploadState.Success(result.data)
                    loadBooks()
                }
                is OperationResult.Error -> {
                    _uploadState.value = UploadState.Error(result.message)
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        loadBooks()
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}

sealed class UploadState {
    object Idle : UploadState()
    data class Loading(val progress: Float) : UploadState()
    data class Success(val book: Book) : UploadState()
    data class Error(val message: String) : UploadState()
}