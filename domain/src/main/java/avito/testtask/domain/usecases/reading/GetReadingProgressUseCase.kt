package avito.testtask.domain.usecases.reading

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.ReadingProgress
import avito.testtask.domain.repos.BookReository

class GetReadingProgressUseCase(
    private val bookRepository: BookReository
) {
    suspend fun execute(bookId: String): OperationResult<ReadingProgress> {
        return bookRepository.getReadingProgress(bookId)
    }
}