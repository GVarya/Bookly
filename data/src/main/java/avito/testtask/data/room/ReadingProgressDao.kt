package avito.testtask.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import avito.testtask.data.models.LocalReadingProggress

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId AND userId = :userId")
    suspend fun getProgress(bookId: String, userId: String): LocalReadingProggress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LocalReadingProggress)

    @Query("DELETE FROM reading_progress WHERE bookId = :bookId AND userId = :userId")
    suspend fun deleteProgress(bookId: String, userId: String)
}