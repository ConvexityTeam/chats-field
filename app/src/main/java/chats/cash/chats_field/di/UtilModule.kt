package chats.cash.chats_field.di

import chats.cash.chats_field.utils.PreferenceUtil
import chats.cash.chats_field.utils.PreferenceUtilInterface
import chats.cash.chats_field.utils.network.NetworkStatusTracker
import chats.cash.chats_field.views.core.strings.UiText
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilModule = module {
    single<PreferenceUtilInterface> { PreferenceUtil(androidContext()) }
    single { UiText(get()) }

    single {
        NetworkStatusTracker(androidContext())
    }
}
