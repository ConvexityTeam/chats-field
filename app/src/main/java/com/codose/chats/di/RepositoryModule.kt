package com.codose.chats.di

import com.codose.chats.network.NetworkRepository
import com.codose.chats.offline.BeneficiaryDatabase
import com.codose.chats.offline.OfflineRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
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
