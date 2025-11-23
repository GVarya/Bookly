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

//    companion object {
//        @Volatile
//        private var INSTANCE: BookDatabase? = null
//
//        fun getInstance(context: Context): BookDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    BookDatabase::class.java,
//                    "book_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}

fun createBookDatabase(context: Context): BookDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        BookDatabase::class.java,
        "book_database"
    ).build()
}