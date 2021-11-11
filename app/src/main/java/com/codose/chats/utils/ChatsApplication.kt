package com.codose.chats.utils

import android.app.Application
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import com.codose.chats.BuildConfig
import com.codose.chats.offline.BeneficiaryDatabase
import com.codose.chats.utils.di.apiModule
import com.codose.chats.utils.di.repositoryModule
import com.codose.chats.utils.di.retrofitModule
import com.codose.chats.utils.di.viewModelModule
import com.pixplicity.easyprefs.library.Prefs
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import kotlinx.coroutines.InternalCoroutinesApi
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
            modules(listOf(repositoryModule, viewModelModule, retrofitModule, apiModule))
        }
        initPref()
        initNetworkChecker()
    }

    private fun initNetworkChecker() {
        InternetAvailabilityChecker.init(this)
    }

    private fun initPref() {
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
    }
}