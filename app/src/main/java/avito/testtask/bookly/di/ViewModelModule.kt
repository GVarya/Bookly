package avito.testtask.bookly.di

import avito.testtask.bookly.viewmodels.AuthViewModel
import avito.testtask.bookly.viewmodels.BooksViewModel
import avito.testtask.bookly.viewmodels.ProfileViewModel
import avito.testtask.bookly.viewmodels.ReadingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get(), get(), get()) }
    viewModel { BooksViewModel(get(), get(), get(), get(), get()) }
    viewModel { ReadingViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
}