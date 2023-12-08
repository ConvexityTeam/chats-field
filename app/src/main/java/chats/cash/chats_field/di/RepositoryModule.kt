package chats.cash.chats_field.di

import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.api.imageServices.ImageServiceProvider
import chats.cash.chats_field.network.api.imageServices.ImageServiceProviderImpl
import chats.cash.chats_field.network.api.videoServices.VideoServiceProvider
import chats.cash.chats_field.network.api.videoServices.VideoServiceProviderImpl
import chats.cash.chats_field.network.repository.auth.AuthRepository
import chats.cash.chats_field.network.repository.auth.AuthRepositoryInterface
import chats.cash.chats_field.network.repository.impact_report.ImpactReport
import chats.cash.chats_field.network.repository.impact_report.ImpactReportImpl
import chats.cash.chats_field.offline.BeneficiaryDatabase
import chats.cash.chats_field.offline.OfflineRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { BeneficiaryDatabase.getDatabase(androidContext()) }
    single { get<BeneficiaryDatabase>().beneficiaryDao() }
    single { OfflineRepository(get()) }
    single<AuthRepositoryInterface> { AuthRepository(get(), get()) }
    single<ImageServiceProvider> { ImageServiceProviderImpl(get(), get(), get()) }
    single<VideoServiceProvider> { VideoServiceProviderImpl(get()) }
    single<ImpactReport> {
        ImpactReportImpl(get(), get())
    }
    single {
        NetworkRepository(
            api = get(),
            ninApi = get(),
            offlineRepository = get(),
            context = get(),
            preferenceUtil = get(),
        )
    }
}
