package avito.testtask.data

import android.net.Uri
import avito.testtask.domain.models.Book
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.ReadingProgress
import avito.testtask.domain.repos.BookReository

class BookRepositoryImpl : BookReository {
    override suspend fun loadAllBooks(): OperationResult<List<Book>> {
        TODO("Not yet implemented")
    }

    override suspend fun downloadBook(book: Book): OperationResult<Book> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadBook(
        fileUri: Uri,
        title: String,
        author: String
    ): OperationResult<Book> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBook(book: Book): OperationResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun searchBook(query: String): OperationResult<List<Book>> {
        TODO("Not yet implemented")
    }

    override suspend fun getBookContent(book: Book): OperationResult<String> {
        TODO("Not yet implemented")
    }

    override suspend fun saveReadingProgress(progress: ReadingProgress): OperationResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getReadingProgress(bookId: String): OperationResult<ReadingProgress> {
        TODO("Not yet implemented")
    }
}