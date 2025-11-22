package avito.testtask.domain.usecases.books

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.repos.BookReository

class UploadBookUseCase(
    private val bookRepository: BookReository
) {
    suspend fun execute(fileUri: String, title: String, author: String): OperationResult<Book> {
        return bookRepository.uploadBook(fileUri, title, author)
    }
}