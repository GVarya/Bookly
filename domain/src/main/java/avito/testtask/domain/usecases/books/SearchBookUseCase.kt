package avito.testtask.domain.usecases.books

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.BookReository

class SearchBookUseCase(
    private val bookRepository: BookReository
) {
    suspend fun execute(query: String): OperationResult<List<Book>> {
        return bookRepository.searchBook(query)
    }
}