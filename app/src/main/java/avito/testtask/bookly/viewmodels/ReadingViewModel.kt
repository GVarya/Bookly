package avito.testtask.bookly.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.ReadingProgress
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
    private val saveReadingProgressUseCase: SaveReadingProggressUseCase
) : ViewModel() {

    private val _bookContent = MutableStateFlow<OperationResult<String>?>(null)
    val bookContent: StateFlow<OperationResult<String>?> = _bookContent.asStateFlow()

    private val _readingProgress = MutableStateFlow<ReadingProgress?>(null)
    val readingProgress: StateFlow<ReadingProgress?> = _readingProgress.asStateFlow()

    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadBookContent(book: Book) {
        viewModelScope.launch {
            _currentBook.value = book
            _isLoading.value = true

            _bookContent.value = getBookContentUsecase.execute(book)

            when (val progressResult = getReadingProgressUseCase.execute(book.id)) {
                is OperationResult.Success -> {
                    _readingProgress.value = progressResult.data
                }
                is OperationResult.Error -> {
                    _readingProgress.value = ReadingProgress(bookId = book.id, progress = 0f)
                }
                else -> {}
            }

            _isLoading.value = false
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