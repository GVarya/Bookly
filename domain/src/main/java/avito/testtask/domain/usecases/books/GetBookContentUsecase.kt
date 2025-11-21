package avito.testtask.domain.usecases.books

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.BookReository

class GetBookContentUsecase(
    private val bookRepository: BookReository
) {
    suspend fun execute(book: Book): OperationResult<String> {
        return bookRepository.getBookContent(book)
    }
}