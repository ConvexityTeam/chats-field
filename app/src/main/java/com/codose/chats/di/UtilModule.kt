package com.codose.chats.di

import com.codose.chats.utils.PreferenceUtil
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilModule = module {
    single { PreferenceUtil(androidContext()) }
}
