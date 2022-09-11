package com.codose.chats.di

import android.content.Context
import com.codose.chats.network.NetworkRepository
import com.codose.chats.offline.BeneficiaryDao
import com.codose.chats.offline.BeneficiaryDatabase
import com.codose.chats.offline.OfflineRepository
import org.koin.dsl.module

val repositoryModule = module {

    fun provideDatabase(context : Context) : BeneficiaryDatabase {
        return BeneficiaryDatabase.getDatabase(context)
    }

    fun provideBeneficiaryDao(database: BeneficiaryDatabase) : BeneficiaryDao {
        return database.beneficiaryDao()
    }

    single { provideDatabase(get()) }
    single { provideBeneficiaryDao(get()) }
    single { OfflineRepository(get()) }
    single { NetworkRepository(get(), get(), get()) }
}