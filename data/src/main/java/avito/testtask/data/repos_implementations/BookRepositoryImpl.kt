package avito.testtask.data.repos_implementations

import android.content.Context
import android.os.Environment
import androidx.compose.animation.EnterTransition.Companion.None
import androidx.core.content.ContextCompat
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
import java.io.File
import java.util.UUID
import kotlin.collections.map
import androidx.core.net.toUri
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import kotlin.coroutines.resume

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val readingProgressDao: ReadingProgressDao,
    private val context: Context
) : BookReository {

    private val bucketName = "bookly-bucket"
    private val accessKey = "YCAJEzUZXA-othjXJFOunGoB2"
    private val secretKey = "YCM3aSjGhvonSIBF5oDRBHMPeE-dGIf9PJX9rKMa" // ОЧЕНЬ ПЛОХО, ИСПРАВИТЬ

    private val currentUserId: String get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val s3Client: AmazonS3Client by lazy {
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        AmazonS3Client(credentials).apply {
            setEndpoint("https://storage.yandexcloud.net")
        }
    }

    private val transferUtility: TransferUtility by lazy {
        TransferUtility.builder()
            .context(context)
            .s3Client(s3Client)
            .defaultBucket(bucketName)
            .build()
    }

    override suspend fun loadAllBooks(): OperationResult<List<Book>> {
        return try {
            val localBooks = bookDao.getBooksByUser(currentUserId)
            val books = localBooks.map { it.toBook() }
            OperationResult.Success(books)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to load books")
        }
    }

    override suspend fun downloadBook(book: Book): OperationResult<Book> {

        return try {
            val downloadsDir = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS).first()
            val fileExtension = when (book.bookFormat) {
                BookFormat.TXT -> "txt"
                BookFormat.EPUB -> "epub"
                BookFormat.PDF -> "pdf"
            }
            val localFile = File(downloadsDir, "${book.id}.$fileExtension")

            val remotePath = "books/$currentUserId/${book.id}.$fileExtension"

            val downloadSuccess = suspendCancellableCoroutine<Boolean> { continuation ->
                val downloadObserver = transferUtility.download(remotePath, localFile)

                downloadObserver.setTransferListener(object : TransferListener {
                    override fun onStateChanged(id: Int, state: TransferState) {
                        when (state) {
                            TransferState.COMPLETED -> {
                                continuation.resume(true)
                            }
                            TransferState.FAILED -> {
                                continuation.resume(false)
                            }
                            else -> {
                            }
                        }
                    }

                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    }

                    override fun onError(id: Int, ex: Exception) {
                        continuation.resume(false) { cause, _, _ -> onCancellation(cause) }
                    }

                    private fun onCancellation(cause: Throwable) {}
                })
            }

            if (downloadSuccess) {
                val localBook = book.toLocalBook().copy(localPath = localFile.absolutePath)
                bookDao.insertBook(localBook)
                OperationResult.Success(book.copy(localUrl = localFile.absolutePath))
            } else {
                OperationResult.Error("Download failed")
            }
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
                "txt" -> BookFormat.TXT
                else -> None
            }
            if (bookFormat == None){
                throw Exception("Wrong file format")
            }

            val remotePath = "books/$userId/$bookId.$fileExtension"
            val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri.toUri())

            if (inputStream != null) {
                val tempFile = File.createTempFile("upload_", ".$fileExtension", context.cacheDir)
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }

                val uploadSuccess = suspendCancellableCoroutine<Boolean> { continuation ->
                    val uploadObserver = transferUtility.upload(remotePath, tempFile)

                    uploadObserver.setTransferListener(object : TransferListener {
                        override fun onStateChanged(id: Int, state: TransferState) {
                            when (state) {
                                TransferState.COMPLETED -> {
                                    continuation.resume(true)
                                }
                                TransferState.FAILED -> {
                                    continuation.resume(false)
                                }
                                else -> {
                                }
                            }
                        }

                        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                        }

                        override fun onError(id: Int, ex: Exception) {
                            continuation.resume(false)
                        }
                    })
                }

                tempFile.delete()

                if (uploadSuccess) {
                    val fileUrl = "https://${bucketName}.storage.yandexcloud.net/$remotePath"

                    val book = Book(
                        id = bookId,
                        title = title,
                        author = author,
                        fileUrl = fileUrl,
                        userId = userId,
                        bookFormat = bookFormat as BookFormat,
                        localUrl = null,
                        posterImageUrl = null
                    )

                    val localBook = book.toLocalBook()
                    bookDao.insertBook(localBook)

                    OperationResult.Success(book)
                } else {
                    OperationResult.Error("Upload failed")
                }
            } else {
                OperationResult.Error("Failed to open file stream")
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Upload failed")
        }
    }

    override suspend fun deleteBook(book: Book): OperationResult<Unit> {

        return try {
            book.localUrl?.let { localPath ->
                File(localPath).delete()
            }

            bookDao.deleteBook(book.id)

            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Delete failed")
        }
    }

    override suspend fun searchBook(query: String): OperationResult<List<Book>> {

        return try {
            val localBooks = bookDao.searchBooks(query).map { it.toBook() }
            OperationResult.Success(localBooks)
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