package chats.cash.chats_field.di

import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.offline.BeneficiaryDatabase
import chats.cash.chats_field.offline.OfflineRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { BeneficiaryDatabase.getDatabase(androidContext()) }
    single { get<BeneficiaryDatabase>().beneficiaryDao() }
    single { OfflineRepository(get()) }
    single {
        NetworkRepository(
            api = get(),
            ninApi = get(),
            offlineRepository = get(),
            context = get(),
            preferenceUtil = get()
        )
    }
}
