package avito.testtask.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import avito.testtask.data.models.LocalBook

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE userId = :userId")
    suspend fun getBooksByUser(userId: String): List<LocalBook>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: LocalBook)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: String)

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): LocalBook?

    @Query("SELECT * FROM books WHERE (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%')")
    suspend fun searchBooks(query: String): List<LocalBook>


}