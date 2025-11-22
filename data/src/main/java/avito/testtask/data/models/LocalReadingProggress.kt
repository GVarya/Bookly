package avito.testtask.data.models

import androidx.room.Entity
import avito.testtask.domain.models.ReadingProgress


@Entity(
    tableName = "reading_progress",
    primaryKeys = ["bookId", "userId"]
)
data class LocalReadingProggress(
    val bookId: String,
    val userId: String,
    val progress: Float,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

fun LocalReadingProggress.toReadingProgress(): ReadingProgress {
    return ReadingProgress(
        bookId = bookId,
        progress = progress,
        currentPage = currentPage,
        totalPages = totalPages
    )
}

fun ReadingProgress.toLocalReadingProgress(userId: String): LocalReadingProggress {
    return LocalReadingProggress(
        bookId = bookId,
        userId = userId,
        progress = progress,
        currentPage = currentPage,
        totalPages = totalPages
    )
}