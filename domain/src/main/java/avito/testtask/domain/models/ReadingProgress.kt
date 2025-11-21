package avito.testtask.domain.models

data class ReadingProgress(
    val bookId: String,
    val progress: Float,          // от 0.0 до 1.0
    val currentPage: Int = 0,
    val totalPages: Int = 0,
)