package avito.testtask.bookly.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.ReadingProgress
import avito.testtask.domain.usecases.books.GetBookByIdUseCase
import avito.testtask.domain.usecases.books.GetBookContentUsecase
import avito.testtask.domain.usecases.reading.GetReadingProgressUseCase
import avito.testtask.domain.usecases.reading.SaveReadingProggressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReadingViewModel(
    private val getBookContentUsecase: GetBookContentUsecase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
    private val saveReadingProgressUseCase: SaveReadingProggressUseCase,
    private val getBookByIdUseCase: GetBookByIdUseCase
) : ViewModel() {

    private val _bookContent = MutableStateFlow<OperationResult<String>?>(null)
    val bookContent: StateFlow<OperationResult<String>?> = _bookContent.asStateFlow()

    private val _readingProgress = MutableStateFlow<ReadingProgress?>(null)
    val readingProgress: StateFlow<ReadingProgress?> = _readingProgress.asStateFlow()

    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadBookById(bookId: String) {
        viewModelScope.launch {
            Log.d("DEBUG: Starting to load book with ID", bookId)
            _isLoading.value = true
            _bookContent.value = null

            try {
                when (val result = getBookByIdUseCase.execute(bookId)) {
                    is OperationResult.Success -> {
                        Log.d("DEBUG: Book loaded successfully", result.data.title)
                        _currentBook.value = result.data
                        loadBookContent(result.data)
                    }
                    is OperationResult.Error -> {
                        Log.d("DEBUG: Error loading book", result.message.toString())
                        _bookContent.value = OperationResult.Error(result.message)
                        _isLoading.value = false
                    }
                    else -> {
                        Log.d("DEBUG: Unknown result type", "")
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.d("DEBUG: Exception in loadBookById", e.message.toString())
                _bookContent.value = OperationResult.Error(e.message.toString())
                _isLoading.value = false
            }
        }
    }
    fun loadBookContent(book: Book) {
        viewModelScope.launch {
            Log.d("DEBUG: Loading content for book", "${book.title}, format: ${book.bookFormat}")
            _isLoading.value = true

            try {
                val contentResult = getBookContentUsecase.execute(book)
                Log.d("DEBUG: Content result", contentResult.toString())
                _bookContent.value = contentResult

                when (val progressResult = getReadingProgressUseCase.execute(book.id)) {
                    is OperationResult.Success -> {
                        _readingProgress.value = progressResult.data
                        Log.d("DEBUG: Progress loaded", progressResult.data.progress.toString())
                    }
                    is OperationResult.Error -> {
                        _readingProgress.value = ReadingProgress(bookId = book.id, progress = 0f)
                        Log.d("DEBUG: Using default progress", "")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.d("DEBUG: Exception in loadBookContent", e.message.toString())
                _bookContent.value = OperationResult.Error(e.message.toString())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveProgress(progress: Float, currentPage: Int = 0, totalPages: Int = 0) {
        viewModelScope.launch {
            _currentBook.value?.let { book ->
                val newProgress = ReadingProgress(
                    bookId = book.id,
                    progress = progress,
                    currentPage = currentPage,
                    totalPages = totalPages
                )
                _readingProgress.value = newProgress
                saveReadingProgressUseCase.execute(newProgress)
            }
        }
    }

    fun updateProgress(progress: Float) {
        _readingProgress.value?.let { currentProgress ->
            saveProgress(progress, currentProgress.currentPage, currentProgress.totalPages)
        }
    }
}