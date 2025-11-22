package avito.testtask.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import avito.testtask.domain.models.Book
import avito.testtask.domain.models.BookFormat

@Entity(tableName = "books")
data class LocalBook(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val fileUrl: String,
    val localPath: String?,
    val bookFormat: String,
    val userId: String,
    val posterImageUrl: String?,
    val downloadedAt: Long = System.currentTimeMillis()
)

fun LocalBook.toBook(): Book {
    return Book(
        id = id,
        title = title,
        author = author,
        fileUrl = fileUrl,
        localUrl = localPath,
        bookFormat = BookFormat.valueOf(bookFormat),
        userId = userId,
        posterImageUrl = posterImageUrl
    )
}

fun Book.toLocalBook(): LocalBook {
    return LocalBook(
        id = id,
        title = title,
        author = author,
        fileUrl = fileUrl,
        localPath = localUrl,
        bookFormat = bookFormat.name,
        userId = userId,
        posterImageUrl = posterImageUrl
    )
}