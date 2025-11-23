package avito.testtask.bookly.di

import avito.testtask.domain.usecases.auth.GetCurrentUserUseCase
import avito.testtask.domain.usecases.auth.LogOutUsecase
import avito.testtask.domain.usecases.auth.LoginUsecase
import avito.testtask.domain.usecases.auth.RegisterUseCase
import avito.testtask.domain.usecases.books.DeleteBookUseCase
import avito.testtask.domain.usecases.books.DownloadBookUseCase
import avito.testtask.domain.usecases.books.GetAllBooksUseCase
import avito.testtask.domain.usecases.books.GetBookByIdUseCase
import avito.testtask.domain.usecases.books.GetBookContentUsecase
import avito.testtask.domain.usecases.books.SearchBookUseCase
import avito.testtask.domain.usecases.books.UploadBookUseCase
import avito.testtask.domain.usecases.reading.GetReadingProgressUseCase
import avito.testtask.domain.usecases.reading.SaveReadingProggressUseCase
import avito.testtask.domain.usecases.user.GetUserUseCase
import avito.testtask.domain.usecases.user.UpdateAvatarImageUsecase
import avito.testtask.domain.usecases.user.UpdateUserUsecase
import org.koin.dsl.module

val useCasesModule = module {
    factory { LoginUsecase(get()) }
    factory { RegisterUseCase(get()) }
    factory { LogOutUsecase(get()) }
    factory { GetCurrentUserUseCase(get()) }

    factory { GetAllBooksUseCase(get()) }
    factory { DownloadBookUseCase(get()) }
    factory { UploadBookUseCase(get()) }
    factory { DeleteBookUseCase(get()) }
    factory { SearchBookUseCase(get()) }
    factory { GetBookContentUsecase(get()) }
    factory { GetBookByIdUseCase(get()) }


    factory { GetReadingProgressUseCase(get()) }
    factory { SaveReadingProggressUseCase(get()) }

    factory { GetUserUseCase(get()) }
    factory { UpdateUserUsecase(get()) }
    factory { UpdateAvatarImageUsecase(get()) }
}