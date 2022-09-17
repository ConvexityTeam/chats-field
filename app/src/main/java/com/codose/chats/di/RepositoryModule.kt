package com.codose.chats.di

import android.content.Context
import com.codose.chats.network.NetworkRepository
import com.codose.chats.offline.BeneficiaryDao
import com.codose.chats.offline.BeneficiaryDatabase
import com.codose.chats.offline.OfflineRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    fun provideDatabase(context: Context): BeneficiaryDatabase {
        return BeneficiaryDatabase.getDatabase(context)
    }

    fun provideBeneficiaryDao(database: BeneficiaryDatabase): BeneficiaryDao {
        return database.beneficiaryDao()
    }

    single { BeneficiaryDatabase.getDatabase(androidContext()) }
    single { get<BeneficiaryDatabase>().beneficiaryDao() }
    single { OfflineRepository(get()) }
    single {
        NetworkRepository(
            api = get(),
            offlineRepository = get(),
            context = get(),
            preferenceUtil = get()
        )
    }
}
