package chats.cash.chats_field

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import chats.cash.chats_field.di.*
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class ChatsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@ChatsApplication)
            modules(
                repositoryModule,
                viewModelModule,
                retrofitModule,
                apiModule,
                utilModule
            )
        }
        initNetworkChecker()
    }

    private fun initNetworkChecker() {
        InternetAvailabilityChecker.init(this)
    }
}
