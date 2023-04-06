package chats.cash.chats_field.network.api

import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.response.campaign.GetAllCampaignsResponse
import chats.cash.chats_field.views.core.strings.UiText
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.util.Collections
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class ConvexityApiTest {

    lateinit var mockWebServer:MockWebServer
    lateinit var convexityApiService: ConvexityApiService
    lateinit var retrofitDataSource: RetrofitDataSource

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val okHttpClientBuilder = OkHttpClient.Builder()
        okHttpClientBuilder
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .protocols( Collections.singletonList(Protocol.HTTP_1_1) )
        val client = okHttpClientBuilder.build()
        convexityApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(client)
            .build().create(ConvexityApiService::class.java)
        retrofitDataSource = RetrofitDataSource(convexityApiService, "Unknown error")
    }



    @Test
    fun testSomething() = runTest {
        val mockResponse  = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = Gson().fromJson("""{
    "code": 200,
    "status": "success",
    "message": "Campaign retrieved",
    "data": [
        {
            "id": 351,
            "OrganisationId": 37,
            "formId": null,
            "title": "Iftar food sharing",
            "minting_limit": 0,
            "is_processing": true,
            "type": "campaign",
            "spending": "vendor",
            "collection_hash": null,
            "description": "description",
            "status": "active",
            "is_funded": false,
            "is_public": false,
            "funded_with": null,
            "budget": 2000,
            "contractIndex": null,
            "amount_disbursed": 0,
            "location": "[]",
            "start_date": "2023-04-05T00:00:00.000Z",
            "paused_date": null,
            "end_date": "2023-04-29T00:00:00.000Z",
            "createdAt": "2023-04-04T04:36:21.888Z",
            "updatedAt": "2023-04-04T04:38:25.120Z",
            "Organisation": {
                "id": 37,
                "name": "DanbabaWorld",
                "email": "jeminusi.j@test3.com",
                "phone": null,
                "address": null,
                "state": null,
                "country": null,
                "logo_link": null,
                "website_url": "http://test3.com",
                "registration_id": "CHATSORG9IX226T",
                "year_of_inception": null,
                "profile_completed": false,
                "is_verified": false,
                "createdAt": "2023-01-19T20:10:04.635Z",
                "updatedAt": "2023-01-19T20:10:04.635Z"
            },
            "ck8": "BN#z2J7A4w*4AP%TkX)X)w"
        },
        {
            "id": 350,
            "OrganisationId": 37,
            "formId": null,
            "title": "Iftar share",
            "minting_limit": 0,
            "is_processing": true,
            "type": "campaign",
            "spending": "vendor",
            "collection_hash": null,
            "description": "description",
            "status": "active",
            "is_funded": false,
            "is_public": false,
            "funded_with": null,
            "budget": 2000,
            "contractIndex": null,
            "amount_disbursed": 0,
            "location": "[]",
            "start_date": "2023-04-05T00:00:00.000Z",
            "paused_date": null,
            "end_date": "2023-04-29T00:00:00.000Z",
            "createdAt": "2023-04-04T04:19:17.145Z",
            "updatedAt": "2023-04-04T04:23:40.246Z",
            "Organisation": {
                "id": 37,
                "name": "DanbabaWorld",
                "email": "jeminusi.j@test3.com",
                "phone": null,
                "address": null,
                "state": null,
                "country": null,
                "logo_link": null,
                "website_url": "http://test3.com",
                "registration_id": "CHATSORG9IX226T",
                "year_of_inception": null,
                "profile_completed": false,
                "is_verified": false,
                "createdAt": "2023-01-19T20:10:04.635Z",
                "updatedAt": "2023-01-19T20:10:04.635Z"
            },
            "ck8": "U+6MtbbCt)vBfY5by@mlxBb"
        }
    ]
}""",GetAllCampaignsResponse::class.java)
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = retrofitDataSource.getAllCampaigns(3,"")
        mockWebServer.takeRequest()

        println(response.toString())
        Assert.assertTrue(response.first() is NetworkResponse.Loading)
    }

    @After
    fun afterTest() {
        mockWebServer.shutdown()
    }
}