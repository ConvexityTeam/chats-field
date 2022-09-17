package com.codose.chats

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.codose.chats.di.*
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
            modules(listOf(repositoryModule, viewModelModule, retrofitModule, apiModule, utilModule))
        }
        initNetworkChecker()
    }

    private fun initNetworkChecker() {
        InternetAvailabilityChecker.init(this)
    }
}
