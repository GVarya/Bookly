package avito.testtask.domain.usecases.books

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.BookReository

class DeleteBookUseCase(
    private val bookRepository: BookReository
) {
    suspend fun execute(book: Book): OperationResult<Unit> {
        return bookRepository.deleteBook(book)
    }
}