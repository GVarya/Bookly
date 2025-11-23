package avito.testtask.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import avito.testtask.data.models.LocalBook
import avito.testtask.data.models.LocalReadingProggress

@Database(
    entities = [LocalBook::class, LocalReadingProggress::class],
    version = 1,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readingProgressDao(): ReadingProgressDao

}

fun createBookDatabase(context: Context): BookDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        BookDatabase::class.java,
        "book_database"
    ).build()
}