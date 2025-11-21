package avito.testtask.domain.usecases.reading

import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.ReadingProgress
import avito.testtask.domain.repos.BookReository

class SaveReadingProggressUseCase(
    private val bookRepository: BookReository
) {
    suspend fun execute(progress: ReadingProgress): OperationResult<Unit> {
        return bookRepository.saveReadingProgress(progress)
    }
}