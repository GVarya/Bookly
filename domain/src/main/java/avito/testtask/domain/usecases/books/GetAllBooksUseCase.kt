package avito.testtask.domain.usecases.books

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.BookReository

class GetAllBooksUseCase (
    private val boorRepository: BookReository

){
    suspend fun execute(): OperationResult<List<Book>> {
        return boorRepository.loadAllBooks()
    }
}