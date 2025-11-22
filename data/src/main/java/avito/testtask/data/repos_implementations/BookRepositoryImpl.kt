package avito.testtask.data.repos_implementations

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import avito.testtask.data.models.RemoteBook
import avito.testtask.data.models.toBook
import avito.testtask.data.models.toLocalBook
import avito.testtask.data.models.toLocalReadingProgress
import avito.testtask.data.models.toReadingProgress
import avito.testtask.data.room.BookDao
import avito.testtask.data.room.ReadingProgressDao
import avito.testtask.domain.models.Book
import avito.testtask.domain.models.BookFormat
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.ReadingProgress
import avito.testtask.domain.repos.BookReository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import kotlin.collections.map
import androidx.core.net.toUri

class BookRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val bookDao: BookDao,
    private val readingProgressDao: ReadingProgressDao,
    private val context: Context
) : BookReository {

    private val currentUserId: String get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override suspend fun loadAllBooks(): OperationResult<List<Book>> {
        return try {
            val firestoreBooks = firestore.collection("books")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()
                .toObjects(RemoteBook::class.java)

            val localBooks = bookDao.getBooksByUser(currentUserId).associateBy { it.id }

            val books = firestoreBooks.map { remoteBoor ->
                val localBook = localBooks[remoteBoor.id]
                remoteBoor.toBook(localBook?.localPath)
            }

            OperationResult.Success(books)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to load books")
        }    }

    override suspend fun downloadBook(book: Book): OperationResult<Book> {
        return try {
            val downloadsDir = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS).first()
            val fileExtension = when (book.bookFormat) {
                BookFormat.TXT -> "txt"
                BookFormat.EPUB -> "epub"
                BookFormat.PDF -> "pdf"
            }
            val localFile = File(downloadsDir, "${book.id}.$fileExtension")

            val storageRef = storage.getReferenceFromUrl(book.fileUrl)
            storageRef.getFile(localFile).await()

            val localBook = book.toLocalBook().copy(localPath = localFile.absolutePath)
            bookDao.insertBook(localBook)

            OperationResult.Success(book.copy(localUrl = localFile.absolutePath))
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Download failed")
        }
    }

    override suspend fun uploadBook(
        fileUri: String,
        title: String,
        author: String
    ): OperationResult<Book> {
        return try {
            val userId = currentUserId
            val bookId = UUID.randomUUID().toString()
            val fileExtension = context.contentResolver.getType(fileUri.toUri())?.split("/")?.last() ?: "txt"
            val bookFormat = when (fileExtension) {
                "pdf" -> BookFormat.PDF
                "epub" -> BookFormat.EPUB
                else -> BookFormat.TXT
            }

            val storageRef = storage.reference.child("books/$userId/$bookId.$fileExtension")
            val uploadTask = storageRef.putFile(fileUri.toUri()).await()
            val downloadUrl = storageRef.downloadUrl.await()

            val book = RemoteBook(
                id = bookId,
                title = title,
                author = author,
                fileUrl = downloadUrl.toString(),
                userId = userId,
                bookFormat = bookFormat.name
            )

            firestore.collection("books")
                .document(bookId)
                .set(book)
                .await()

            OperationResult.Success(book.toBook())
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Upload failed")
        }
    }

    override suspend fun deleteBook(book: Book): OperationResult<Unit> {
        return try {

            book.localUrl?.let { localPath ->
                File(localPath).delete()
                bookDao.deleteBook(book.id)
            }

            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Delete failed")
        }
    }

    override suspend fun searchBook(query: String): OperationResult<List<Book>> {
        return try {
            val localBooks = bookDao.searchBooks(query).map { it.toBook() }

            if (localBooks.isEmpty()) {
                val firestoreBooks = firestore.collection("books")
                    .whereEqualTo("userId", currentUserId)
                    .whereGreaterThanOrEqualTo("title", query)
                    .whereLessThanOrEqualTo("title", query + "\uf8ff")
                    .get()
                    .await()
                    .toObjects(RemoteBook::class.java)
                    .map { it.toBook() }

                OperationResult.Success(firestoreBooks)
            } else {
                OperationResult.Success(localBooks)
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Search failed")
        }
    }

    override suspend fun getBookContent(book: Book): OperationResult<String> {
        return try {
            val localPath = book.localUrl
            if (localPath.isNullOrEmpty()) {
                return OperationResult.Error("Book is not downloaded locally")
            }

            val file = File(localPath)
            if (!file.exists()) {
                return OperationResult.Error("Local book file not found")
            }
            val content = when (book.bookFormat) {
                BookFormat.TXT -> file.readText()
                BookFormat.PDF, BookFormat.EPUB -> {
                    localPath
                }
            }

            OperationResult.Success(content)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to read book content")
        }
    }

    override suspend fun saveReadingProgress(progress: ReadingProgress): OperationResult<Unit> {
        return try {
            val localProgress = progress.toLocalReadingProgress(currentUserId)
            readingProgressDao.insertProgress(localProgress)
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to save progress")
        }
    }

    override suspend fun getReadingProgress(bookId: String): OperationResult<ReadingProgress> {
        return try {
            val progress = readingProgressDao.getProgress(bookId, currentUserId)
            if (progress != null) {
                OperationResult.Success(progress.toReadingProgress())
            } else {
                OperationResult.Success(ReadingProgress(bookId, 0f))
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to get reading progress")
        }    }
}