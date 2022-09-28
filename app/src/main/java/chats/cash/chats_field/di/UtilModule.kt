package chats.cash.chats_field.di

import chats.cash.chats_field.utils.PreferenceUtil
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilModule = module {
    single { PreferenceUtil(androidContext()) }
}
