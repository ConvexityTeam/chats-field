package chats.cash.chats_field.network.repository.auth

import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.ConvexityApiService
import chats.cash.chats_field.network.api.imageServices.ImageServiceProvider
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.login.AssociatedOrganisation
import chats.cash.chats_field.network.response.login.Data
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.login.Organisation
import chats.cash.chats_field.network.response.login.User
import chats.cash.chats_field.utils.PreferenceUtil
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.util.Collections
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class AuthRepositoryTest {

    lateinit var mockWebServer: MockWebServer
    lateinit var convexityApiService: ConvexityApiService

    lateinit var authRepository: AuthRepository
    lateinit var dataSource: RetrofitDataSource
    val preferenceUtil = mockk<PreferenceUtil>(relaxed = true, relaxUnitFun = true)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val okHttpClientBuilder = OkHttpClient.Builder()
        okHttpClientBuilder
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
        val client = okHttpClientBuilder.build()
        convexityApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(client)
            .build().create(ConvexityApiService::class.java)
        dataSource = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            ImageService(),
            "network",
        )
        authRepository = AuthRepository(dataSource, preferenceUtil)
    }

    inner class ImageService() : ImageServiceProvider {
        override suspend fun uploadImage(
            path: String,
            fileName: String,
            shouldCompressImage: Boolean,
            onProgress: (Float) -> Unit,
        ): Deferred<String?> {
            return CompletableDeferred("hello")
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loginNGO() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        mockResponse.setBody(
            Gson().toJson(
                LoginResponse(
                    200,
                    Data(
                        token = "accusata",
                        user = User(
                            address = "intellegebat",
                            bvn = null,
                            createdAt = "atqui",
                            dob = "adipiscing",
                            email = "irvin.vega@example.com",
                            firstName = "Tammie Kinney",
                            gender = "nostrum",
                            id = 3713,
                            isBvnVerified = false,
                            isEmailVerified = false,
                            isPhoneVerified = false,
                            isPublic = false,
                            isSelfSignup = false,
                            isTfaEnabled = false,
                            lastLogin = null,
                            lastName = "Petra Richards",
                            lat = null,
                            location = "inceptos",
                            long = null,
                            maritalStatus = null,
                            nfc = null,
                            nin = null,
                            password = "tamquam",
                            phone = "(157) 389-3543",
                            pin = null,
                            profilePic = "dis",
                            referalId = null,
                            roleId = 7023,
                            status = "epicurei",
                            tfaSecret = null,
                            updatedAt = "sea",
                            Organisation = null,
                            associatedOrganisations = listOf(
                                AssociatedOrganisation(
                                    Organisation = Organisation(
                                        address = null,
                                        country = null,
                                        createdAt = "accumsan",
                                        email = null,
                                        id = 4253,
                                        is_verified = false,
                                        logo_link = null,
                                        name = "Ollie Cantu",
                                        phone = null,
                                        registration_id = null,
                                        state = null,
                                        updatedAt = "convenire",
                                        website_url = "https://www.google.com/#q=eros",
                                        year_of_inception = null,
                                    ),
                                    OrganisationId = 3254,
                                    UserId = 8388,
                                    createdAt = "homero",
                                    id = 8816,
                                    role = "platonem",
                                    updatedAt = "veritus",
                                ),
                            ),
                        ),
                    ),
                    "success",
                    "success",
                ),
            ),
        )
        mockWebServer.enqueue(mockResponse)
        val body = LoginBody("test", "tule")
        val res = authRepository.loginNGO(body)
        assert(res.first() is NetworkResponse.Loading)
        assert(res.last() is NetworkResponse.Success)
        advanceUntilIdle()
        verify { preferenceUtil.setNGOToken("Bearer accusata") }
        verify { preferenceUtil.setNGO(3254, "Ollie Cantu") }
    }

    @Test
    fun sendForgotEmail() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        mockResponse.setBody(Gson().toJson(ForgotPasswordResponse("success", "200")))
        mockWebServer.enqueue(mockResponse)
        val res = authRepository.sendForgotEmail("simon@gmail.com")
        assert(res.first() is NetworkResponse.Loading)
        assert(res.last() is NetworkResponse.Success)
    }
}
