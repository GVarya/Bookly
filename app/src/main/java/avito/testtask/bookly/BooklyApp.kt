package avito.testtask.bookly

import android.app.Application
import avito.testtask.bookly.di.appModule
import avito.testtask.bookly.di.dataModule
import avito.testtask.bookly.di.useCasesModule
import avito.testtask.bookly.di.viewModelModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

class BooklyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        GlobalContext.startKoin {
            androidContext(this@BooklyApp)
            modules(
                appModule,
                dataModule,
                useCasesModule,
                viewModelModule
            )
        }
        FirebaseApp.initializeApp(this)
    }
}