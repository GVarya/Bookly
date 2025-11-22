package avito.testtask.bookly.di

import android.content.Context
import avito.testtask.data.repos_implementations.AuthRepositoryImpl
import avito.testtask.data.repos_implementations.BookRepositoryImpl
import avito.testtask.data.repos_implementations.UserRepositoryImpl
import avito.testtask.data.room.BookDao
import avito.testtask.data.room.BookDatabase
import avito.testtask.data.room.ReadingProgressDao
import avito.testtask.domain.repos.AuthRepository
import avito.testtask.domain.repos.BookReository
import avito.testtask.domain.repos.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import org.koin.dsl.module

val dataModule = module {

    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }

    single { provideBookDatabase(get()) }
    single { provideBookDao(get()) }
    single { provideReadingProgressDao(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<BookReository> {
        BookRepositoryImpl(
            firestore = get(),
            storage = get(),
            bookDao = get(),
            readingProgressDao = get(),
            context = get()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            firebaseAuth = get(),
            firestore = get(),
            storage = get()
        )
    }
}

private fun provideBookDatabase(context: Context): BookDatabase {
    return BookDatabase.getInstance(context)
}

private fun provideBookDao(database: BookDatabase): BookDao {
    return database.bookDao()
}

private fun provideReadingProgressDao(database: BookDatabase): ReadingProgressDao {
    return database.readingProgressDao()
}