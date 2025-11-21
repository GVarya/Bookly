package avito.testtask.domain.usecases.books

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.BookReository

class DownloadBookUseCase(
    private val bookRepository: BookReository
) {
    suspend fun execute(book: Book): OperationResult<Book> {
        return bookRepository.downloadBook(book)
    }
}