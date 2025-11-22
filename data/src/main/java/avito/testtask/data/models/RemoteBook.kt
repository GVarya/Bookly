package avito.testtask.data.models

import avito.testtask.domain.models.Book
import avito.testtask.domain.models.BookFormat

data class RemoteBook(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val fileUrl: String = "",
    val userId: String = "",
    val bookFormat: String = "",
    val posterImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toBook(localUrl: String? = null): Book {
        return Book(
            id = id,
            title = title,
            author = author,
            fileUrl = fileUrl,
            localUrl = localUrl,
            bookFormat = BookFormat.valueOf(bookFormat),
            userId = userId,
            posterImageUrl = posterImageUrl
        )
    }
}

fun Book.toFirebaseBook(): RemoteBook {
    return RemoteBook(
        id = id,
        title = title,
        author = author,
        fileUrl = fileUrl,
        userId = userId,
        bookFormat = bookFormat.name,
        posterImageUrl = posterImageUrl
    )
}