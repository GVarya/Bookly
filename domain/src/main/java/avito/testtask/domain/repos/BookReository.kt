package avito.testtask.domain.repos

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.ReadingProgress


interface BookReository {
    suspend fun loadAllBooks(): OperationResult<List<Book>>
    suspend fun downloadBook(book: Book): OperationResult<Book>
    suspend fun uploadBook(fileUri: String, title: String, author: String): OperationResult<Book>
    suspend fun deleteBook(book: Book): OperationResult<Unit>
    suspend fun searchBook(query: String): OperationResult<List<Book>>
    suspend fun getBookContent(book: Book): OperationResult<String>
    suspend fun saveReadingProgress(progress: ReadingProgress): OperationResult<Unit>
    suspend fun getReadingProgress(bookId: String): OperationResult<ReadingProgress>
}