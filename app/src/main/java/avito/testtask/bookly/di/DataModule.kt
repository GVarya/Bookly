package avito.testtask.bookly.di

import avito.testtask.data.repos_implementations.AuthRepositoryImpl
import avito.testtask.data.repos_implementations.BookRepositoryImpl
import avito.testtask.data.repos_implementations.UserRepositoryImpl
import avito.testtask.data.room.BookDatabase
import avito.testtask.data.room.createBookDatabase
import avito.testtask.domain.repos.AuthRepository
import avito.testtask.domain.repos.BookReository
import avito.testtask.domain.repos.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }

    single { createBookDatabase(androidContext()) }
    single { get<BookDatabase>().bookDao() }
    single { get<BookDatabase>().readingProgressDao() }



    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<BookReository> {
        BookRepositoryImpl(
            bookDao = get(),
            readingProgressDao = get(),
            context = androidContext()

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

