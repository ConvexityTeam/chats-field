package chats.cash.chats_field.di

import chats.cash.chats_field.R
import chats.cash.chats_field.network.api.AuthInterceptor
import chats.cash.chats_field.network.api.interfaces.BeneficiaryRepositoryInterface
import chats.cash.chats_field.network.api.interfaces.ConvexityDataSourceInterface
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.repository.BeneficiaryRepository
import chats.cash.chats_field.utils.ChatsFieldConstants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

val retrofitModule = module {

    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .disableHtmlEscaping().create()
    }

    fun provideHttpClient(
        interceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClientBuilder
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(authInterceptor)
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
        return okHttpClientBuilder.build()
    }

    single {
        AuthInterceptor(get())
    }

    fun provideRetrofit(
        factory: Gson,
        client: OkHttpClient,
        callAdapterFactory: CoroutineCallAdapterFactory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ChatsFieldConstants.BASE_URL)
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(GsonConverterFactory.create(factory))
            .client(client)
            .build()
    }

    single { provideGson() }
    single { provideHttpClient(get(), get()) }
    single { HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY } }
    single { CoroutineCallAdapterFactory() }
    single { provideRetrofit(get(), get(), get()) }

    single<ConvexityDataSourceInterface> {
        RetrofitDataSource(
            get(),
            androidContext().getString(R.string.an_unknown_error_occured),
            androidContext().getString(
                R.string.pleas_check_your_network_connection_make_sure_you_re_connected_to_a_good_network,
            ),
            get(),
            androidContext()
                .getString(R.string.please_check_your_internet_connection_and_try_again),
        )
    }

    single<BeneficiaryRepositoryInterface> {
        BeneficiaryRepository(
            RetrofitDataSource(
                get(),
                androidContext().getString(R.string.an_unknown_error_occured),
                androidContext().getString(
                    R.string.pleas_check_your_network_connection_make_sure_you_re_connected_to_a_good_network,
                ),
                get(),
                androidContext()
                    .getString(R.string.please_check_your_internet_connection_and_try_again),
            ),
            get(),
            get(),
        )
    }
}
