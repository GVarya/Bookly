package avito.testtask.domain.usecases.books

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.BookReository

class GetBookByIdUseCase (
    private val boorRepository: BookReository
)
{
    suspend fun execute(bookId: String):OperationResult<Book>
    {
        return boorRepository.loadBookById(bookId)
    }
}
