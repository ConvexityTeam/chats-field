package com.codose.chats.di

import com.codose.chats.network.api.ConvexityApiService
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {

    fun provideUseApi(retrofit: Retrofit): ConvexityApiService {
        return retrofit.create(ConvexityApiService::class.java)
    }

    single { provideUseApi(get()) }
}