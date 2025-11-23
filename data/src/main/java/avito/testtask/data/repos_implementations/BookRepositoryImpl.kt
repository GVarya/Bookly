package avito.testtask.data.repos_implementations

import android.content.Context

import android.os.Environment
import android.util.Log
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
import avito.testtask.data.security.SimpleSecretManager
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.coroutines.resume

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val readingProgressDao: ReadingProgressDao,
    private val context: Context
) : BookReository {
    private val secretManager = SimpleSecretManager(context)

    init {
        if (!secretManager.hasSecrets()) {
            secretManager.saveSecrets(
                "YCAJEzUZXA-othjXJFOunGoB2",
                ${{ secrets.API_SECRET_KEY }}
                "bookly-bucket"
            )
        }
    }

    private val currentUserId: String get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val s3Client: AmazonS3Client by lazy {
        val credentials = BasicAWSCredentials(secretManager.getAccessKey()!!,
            secretManager.getSecretKey()!!)
        AmazonS3Client(credentials).apply {
            setEndpoint("https://storage.yandexcloud.net")
        }
    }

    private val transferUtility: TransferUtility by lazy {
        TransferUtility.builder()
            .context(context)
            .s3Client(s3Client)
            .defaultBucket(secretManager.getBucketName()!!)
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

    override suspend fun loadBookById(bookId: String): OperationResult<Book> {
        return try {
            val localBook = bookDao.getBookById(bookId)
            val book = localBook?.toBook()
            if (book != null){
                OperationResult.Success(book)
            }else{
                OperationResult.Error( "Failed to load book")
            }
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "Failed to load book")
        }
    }

    override suspend fun downloadBook(book: Book): OperationResult<Book> {

        return try {
            val downloadsDir = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS).first()
            val fileExtension = when (book.bookFormat) {
                BookFormat.TXT -> "plain"
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
            val fileExtension = context.contentResolver.getType(fileUri.toUri())?.split("/")?.last()
            Log.i("FORMAT", fileExtension?: "none")

            val bookFormat = when (fileExtension) {
                "pdf" -> BookFormat.PDF
                "epub" -> BookFormat.EPUB
                "plain" -> BookFormat.TXT
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
                    val fileUrl = "https://${secretManager.getBucketName()!!}.storage.yandexcloud.net/$remotePath"

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
                BookFormat.PDF -> extractTextFromPdf(file)
                BookFormat.EPUB -> ""
//                BookFormat.EPUB -> extractTextFromEpub(file)
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
    fun extractTextFromPdf(pdfFile: File): String {
        PDFBoxResourceLoader.init(context)
        return try {
            PDDocument.load(pdfFile).use { document ->
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                stripper.setShouldSeparateByBeads(false)
                stripper.lineSeparator = "\n"

                val text = stripper.getText(document)
                cleanExtractedText(text)
            }
        } catch (e: IOException) {
            throw RuntimeException("Ошибка чтения PDF файла: ${e.message}", e)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка обработки PDF: ${e.message}", e)
        }
    }

    private fun cleanExtractedText(text: String): String {
        return text
            .replace(Regex("\\n{3,}"), "\n\n")
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "")
            .trim()
    }

//    fun extractTextFromEpub(epubFile: File): String {
//        return try {
//            val epubReader = EpubReader()
//            val book: Book = epubReader.readEpub(FileInputStream(epubFile))
//
//            val textContent = StringBuilder()
//            var chapterCount = 0
//
//
//            if (isTextContent(book)) {
//                chapterCount++
//                val chapterText = extractTextFromResource(book)
//                if (chapterText.isNotBlank()) {
//                    textContent.append("Глава $chapterCount\n\n")
//                    textContent.append(chapterText)
//                    textContent.append("\n\n")
//                }
//            }
//
//
//            if (textContent.isEmpty()) {
//                extractTextFromEpubAlternative(epubFile)
//            } else {
//                cleanExtractedTextEpub(textContent.toString())
//            }
//        } catch (e: Exception) {
//            throw RuntimeException("Ошибка чтения EPUB файла: ${e.message}", e)
//        }
//    }
//
//    private fun isTextContent(resource: Resource): Boolean {
//        val mediaType = resource.mediaType.name
//        return mediaType.contains("html") ||
//                mediaType.contains("xhtml") ||
//                mediaType.contains("application/xhtml+xml")
//    }
//
//    private fun extractTextFromResource(resource: Resource): String {
//        return try {
//            val content = String(resource.data, Charsets.UTF_8)
//            val doc = Jsoup.parse(content)
//
//            doc.select("script, style, nav, header, footer").remove()
//            val body = doc.body()
//            if (body != null) {
//                body.text()
//            } else {
//                doc.text()
//            }
//        } catch (e: Exception) {
//            ""
//        }
//    }

//    private fun extractTextFromEpubAlternative(epubFile: File): String {
//        return try {
//            val zipFile = ZipFile(epubFile)
//            val textContent = StringBuilder()
//
//            zipFile.entries().iterator().forEach { entry ->
//                if (isHtmlEntry(entry)) {
//                    try {
//                        zipFile.getInputStream(entry).use { inputStream ->
//                            val content = inputStream.readBytes().toString(Charsets.UTF_8)
////                            val doc = Jsoup.parse(content)
//                            doc.select("script, style").remove()
//                            val text = doc.body()?.text() ?: doc.text()
//                            if (text.isNotBlank()) {
//                                textContent.append(text)
//                                textContent.append("\n\n")
//                            }
//                        }
//                    } catch (e: Exception) {
//                    }
//                }
//            }
//
//            zipFile.close()
//            cleanExtractedTextEpub(textContent.toString())
//        } catch (e: Exception) {
//            throw RuntimeException("Альтернативный метод чтения EPUB не сработал: ${e.message}")
//        }
//    }

    private fun isHtmlEntry(entry: ZipEntry): Boolean {
        val name = entry.name.toLowerCase()
        return !name.contains("mimetype") &&
                !name.contains("toc.ncx") &&
                !name.startsWith("meta-inf") &&
                (name.endsWith(".html") ||
                        name.endsWith(".xhtml") ||
                        name.endsWith(".htm"))
    }

    private fun cleanExtractedTextEpub(text: String): String {
        return text
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "")
            .trim()
    }
}