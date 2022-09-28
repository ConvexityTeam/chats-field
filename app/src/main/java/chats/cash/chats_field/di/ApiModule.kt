package chats.cash.chats_field.di

import chats.cash.chats_field.network.api.ConvexityApiService
import chats.cash.chats_field.network.api.NinVerificationApi
import chats.cash.chats_field.utils.BluetoothConstants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {

    fun provideUseApi(retrofit: Retrofit): ConvexityApiService {
        return retrofit.create(ConvexityApiService::class.java)
    }

    fun provideClient() = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().also {
        it.level = HttpLoggingInterceptor.Level.BODY
    }).build()

    single { provideUseApi(get()) }

    single<NinVerificationApi> {
        Retrofit.Builder()
            .baseUrl(BluetoothConstants.NIN_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(provideClient())
            .build()
            .create(NinVerificationApi::class.java)
    }
}
