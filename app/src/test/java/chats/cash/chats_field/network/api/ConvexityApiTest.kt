package chats.cash.chats_field.network.api

import chats.cash.chats_field.model.campaignform.AllCampaignFormResponse
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.imageServices.ImageServiceProvider
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.response.RegisterResponse
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.campaign.GetAllCampaignsResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    lateinit var mockWebServer: MockWebServer
    lateinit var convexityApiService: ConvexityApiService

    private val imageService = ImageService()

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

    @Test
    fun assertGetAllCampaignsFirstEmittionIsLoading() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = Gson().fromJson(dummyRes, GetAllCampaignsResponse::class.java)
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        ).getAllCampaigns(3, "")

        println(response.toString())
        Assert.assertTrue(response.first() is NetworkResponse.Loading)
    }

    @Test
    fun assertGetAllCampaignsLastEmittionIsSuccess() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = Gson().fromJson(dummyRes, GetAllCampaignsResponse::class.java)
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        ).getAllCampaigns(3, "")

        println(response.toString())
        Assert.assertTrue(response.last() is NetworkResponse.Success)
    }

    @Test
    fun assertGetAllCampaignsSuccessValueIsCorrect() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = Gson().fromJson(dummyRes, GetAllCampaignsResponse::class.java)
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .getAllCampaigns(3, "")

        Assert.assertEquals((response.last() as NetworkResponse.Success).body, responseObj.data.data)
    }

    @Test
    fun assertGetAllCampaignFormsFirstEmittionIsLoading() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = Gson().fromJson(dummyResCampaignForm, AllCampaignFormResponse::class.java)
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .getAllCampaignForms(1, "3")
        println(response.toString())
        Assert.assertTrue(response.first() is NetworkResponse.Loading)
    }

    @Test
    fun assertGetAllCampaignFormsLastEmittionIsSuccess() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = Gson().fromJson(dummyResCampaignForm, AllCampaignFormResponse::class.java)
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        ).getAllCampaignForms(3, "")

        println(response.toString())
        Assert.assertTrue(response.last() is NetworkResponse.Success)
    }

    @Test
    fun assertGetAllCampaignFormsSuccessValueIsCorrect() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = Gson().fromJson(dummyResCampaignForm, AllCampaignFormResponse::class.java)
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        ).getAllCampaignForms(3, "")

        Assert.assertEquals((response.last() as NetworkResponse.Success).body, responseObj.data.data)
    }

    @Test
    fun assertGetAllCampaignSurveyFirstEmittionIsLoading() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = CampaignSurveyResponse(
            200,
            CampaignSurveyResponse.CampaignSurveyResponseData(
                1,
                2,
                "",
                1,
                2,
                emptyList(),
                "2sec",
            ),
            "success",
            "200",
        )
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .getCampaignSurvey(1, "3")
        println(response.toString())
        Assert.assertTrue(response.first() is NetworkResponse.Loading)
    }

    @Test
    fun assertGetAllCampaignSurveyLastEmittionIsSuccess() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = CampaignSurveyResponse(
            200,
            CampaignSurveyResponse.CampaignSurveyResponseData(
                1,
                2,
                "",
                1,
                2,
                emptyList(),
                "2sec",
            ),
            "success",
            "200",
        )
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .getCampaignSurvey(3, "")

        println(response.toString())
        Assert.assertTrue(response.last() is NetworkResponse.Success)
    }

    @Test
    fun assertGetAllCampaignSurveySuccessValueIsCorrect() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = CampaignSurveyResponse(
            200,
            CampaignSurveyResponse.CampaignSurveyResponseData(
                1,
                2,
                "",
                1,
                2,
                emptyList(),
                "2sec",
            ),
            "success",
            "200",
        )
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .getCampaignSurvey(3, "")

        println(response.toString())
        Assert.assertEquals(
            (response.last() as NetworkResponse.Success).body,
            responseObj.campaignSurveyResponseData,
        )
    }

    @Test
    fun assertOnboardBeneficiaryFirstEmittionIsLoading() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = "2"
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .OnboardBeneficiary(Beneficiary(), false, 2, "3")
        println(response.toString())
        Assert.assertTrue(response.first() is NetworkResponse.Loading)
    }

    @Test
    fun assertOnboardBeneficiaryLastEmittionIsSuccess() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = RegisterResponse(
            200,
            "2",
            "onboarded",
            "200",
        )
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .OnboardBeneficiary(Beneficiary(), false, 2, "3")

        println(response.toString())
        response.collect {
            if (it is NetworkResponse.Success) {
                Assert.assertTrue((true))
                return@collect
            }
        }
    }

    @Test
    fun assertOnboardBeneficiarySuccessValueIsCorrect() = runTest {
        val mockResponse = MockResponse()
        mockResponse
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseObj = RegisterResponse(
            200,
            "2",
            "onboarded",
            "200",
        )
        mockResponse.setBody(Gson().toJson(responseObj))
        mockWebServer.enqueue(mockResponse)

        val response = RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        )
            .OnboardBeneficiary(Beneficiary(), false, 2, "3")

        response.collect {
            if (it is NetworkResponse.Success) {
                Assert.assertEquals(it.body, "2")
                return@collect
            }
        }
    }

//    @Test
//    fun assertOnboarGroupdBeneficiarySuccessValueIsCorrect() = runTest {
//        val mockResponse = MockResponse()
//        mockResponse
//            .setResponseCode(HttpURLConnection.HTTP_OK)
//        val responseObj = OnboardGroupBeneficiaryResponse(
//            200,
//            OnboardGroupBeneficiaryResponse.OnboardGroupBeneficiaryData(
//                address = "",
//                bvn = "",
//                country = "",
//                createdAt = "curae",
//                currency = "",
//                deviceImei = "",
//                dob = "maluisset",
//                email = "jack.clemons@example.com",
//                firstName = "Earline Pennington",
//                gender = "per",
//                group = OnboardGroupBeneficiaryResponse.OnboardGroupBeneficiaryData.Group(
//                    createdAt = "voluptatibus",
//                    groupCategory = "sale",
//                    groupName = "Nathaniel Jennings",
//                    id = 4498,
//                    representativeId = 6790,
//                    updatedAt = "eu",
//                ),
//                id = 8367,
//                iris = "",
//                isBvnVerified = false,
//                isEmailVerified = false,
//                isNinVerified = false,
//                isPhoneVerified = false,
//                isPublic = false,
//                isSelfSignup = false,
//                isTfaEnabled = false,
//                lastLogin = "",
//                lastName = "Sherman Burton",
//                location = "",
//                maritalStatus = "",
//                members = listOf(),
//                nfc = "",
//                nin = "",
//                password = "natoque",
//                phone = "(856) 500-2931",
//                pin = "",
//                profilePic = "pertinacia",
//                referalId = "",
//                roleId = 4540,
//                status = "deserunt",
//                tfaSecret = "",
//                updatedAt = "dolores",
//                username = "Roseann Reese",
//                vendorId = "",
//            ),
//            "onboarded",
//            "200",
//        )
//        mockResponse.setBody(Gson().toJson(responseObj))
//        mockWebServer.enqueue(mockResponse)
//
//        val response = RetrofitDataSource(
//            convexityApiService,
//            "error",
//            "socket",
//            imageService,
//            "network",
//        )
//            .OnboardGroupBeneficiary(
//                GroupBeneficiaryBody(
//                    "test",
//                    GroupBeneficiaryGroupDetails("hi", "sup"),
//                    listOf(GroupBeneficiaryMember("john", "C:\\Users\\USER-PC\\Downloads\\auth home.png", "12")),
//                    Beneficiary(profilePic = "C:\\Users\\USER-PC\\Downloads\\auth home.png"),
//                    campaignId = 0,
//                ),
//                true,
//                2,
//                "3",
//            )
//
//        response.collect {
//            if (it is NetworkResponse.Success) {
//                Assert.assertEquals(it.body, responseObj.response)
//                return@collect
//            }
//        }
//    }

    @Test
    fun testOnboardVendor_success() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
        val responseBody = VendorOnboardingResponse(200, responseObj, "success", "200")
        mockResponse.setBody(Gson().toJson(responseBody))
        mockWebServer.enqueue(mockResponse)

        // Act
        val response: MutableList<NetworkResponse<VendorOnboardingResponse.VendorResponseData>> =
            mutableListOf()
        RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        ).OnboardVendor(Beneficiary(), true, 2, "token").collect {
            response.add(it)
        }

        // Assert
        assertEquals(2, response.size)
        assertTrue(response[0] is NetworkResponse.Loading)
        assertTrue(response[1] is NetworkResponse.Success)
        val successResponse = response[1] as NetworkResponse.Success
        assertEquals("2", successResponse.body.vendorId)
    }

    @Test
    fun testOnboardVendor_error() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        val responseBody = VendorOnboardingResponse(
            500,
            responseObj,
            "error",
            "500",
        )
        mockResponse.setBody(Gson().toJson(responseBody))
        mockWebServer.enqueue(mockResponse)

        // Act
        val response: MutableList<NetworkResponse<VendorOnboardingResponse.VendorResponseData>> =
            mutableListOf()
        RetrofitDataSource(
            convexityApiService,
            "error",
            "socket",
            imageService,
            "network",
        ).OnboardVendor(Beneficiary(), true, 2, "token").collect {
            response.add(it)
        }

        // Assert
        assertEquals(2, response.size)
        assertTrue(response[0] is NetworkResponse.Loading)
        assertTrue(response[1] is NetworkResponse.Error)
        val errorResponse = response[1] as NetworkResponse.Error
        assertEquals(responseBody.message, errorResponse._message)
    }

    val responseObj = VendorOnboardingResponse.VendorResponseData(
        address = null,
        bvn = null,
        country = null,
        createdAt = null,
        currency = null,
        deviceImei = null,
        dob = null,
        email = null,
        firstName = null,
        gender = null,
        id = 2,
        isBvnVerified = null,
        isEmailVerified = null,
        isNinVerified = null,
        isPhoneVerified = null,
        isPublic = null,
        isSelfSignup = null,
        isTfaEnabled = null,
        lastLogin = null,
        lastName = null,
        location = null,
        maritalStatus = null,
        nfc = null,
        nin = null,
        phone = null,
        profilePic = null,
        referalId = null,
        roleId = null,
        status = null,
        store = null,
        updatedAt = null,
        username = null,
        vendorId = "2",
    )

    @After
    fun afterTest() {
        mockWebServer.shutdown()
    }
}

const val dummyRes = """{
    "code": 200,
    "status": "success",
    "message": "All Campaigns.",
    "data": {
        "totalItems": 125,
        "data": [
            {
                "id": 288,
                "OrganisationId": 28,
                "formId": null,
                "title": "Final Test",
                "minting_limit": 0,
                "is_processing": true,
                "type": "cash-for-work",
                "spending": "all",
                "collection_hash": null,
                "escrow_hash": null,
                "description": "Final Test",
                "status": "ended",
                "is_funded": false,
                "is_public": false,
                "funded_with": null,
                "budget": 4000,
                "contractIndex": null,
                "amount_disbursed": 0,
                "location": null,
                "start_date": "2023-03-17T00:00:00.000Z",
                "paused_date": null,
                "end_date": "2023-03-31T00:00:00.000Z",
                "createdAt": "2023-03-16T08:23:34.627Z",
                "updatedAt": "2023-04-06T10:57:58.199Z",
                "Jobs": [],
                "Beneficiaries": [],
                "ck8": "VSbnS_)zYbvZFpeyw#mG))g",
                "beneficiaries_count": 0,
                "task_count": 0,
                "completed_task": 0
            },
            {
                "id": 284,
                "OrganisationId": 28,
                "formId": 4,
                "title": "ArchyScript test 1",
                "minting_limit": 0,
                "is_processing": true,
                "type": "campaign",
                "spending": "vendor",
                "collection_hash": null,
                "escrow_hash": null,
                "description": "grytyt ArchyScript test 1",
                "status": "ended",
                "is_funded": false,
                "is_public": true,
                "funded_with": null,
                "budget": 45000,
                "contractIndex": null,
                "amount_disbursed": 0,
                "location": null,
                "start_date": "2023-03-14T00:00:00.000Z",
                "paused_date": null,
                "end_date": "2023-03-31T00:00:00.000Z",
                "createdAt": "2023-03-13T16:14:31.296Z",
                "updatedAt": "2023-06-20T18:58:57.509Z",
                "Jobs": [],
                "Beneficiaries": [
                    {
                        "id": 473,
                        "email": "inimdrew@gmail.org",
                        "phone": "08445896671",
                        "first_name": "Inim",
                        "last_name": "Andrew",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{ \"country\": \"Nigeria\", \"coordinates\": [3.234222,23.234444]}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-473-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "1996-03-02T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 473,
                            "CampaignId": 284,
                            "approved": true,
                            "rejected": false,
                            "source": "field app",
                            "createdAt": "2023-05-31T11:59:16.512Z",
                            "updatedAt": "2023-05-31T11:59:16.512Z"
                        }
                    },
                    {
                        "id": 369,
                        "email": "1681259407722@gmail.com",
                        "phone": "2347041846098",
                        "first_name": "John",
                        "last_name": "For",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.954064846038818,4.793344974517822],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-369-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 369,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:52:46.462Z",
                            "updatedAt": "2023-06-14T12:52:46.462Z"
                        }
                    },
                    {
                        "id": 63,
                        "email": "1671722998357@gmail.com",
                        "phone": "+2347123451698",
                        "first_name": "Tire",
                        "last_name": "Tiring",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[3.4064478874206543,6.4654221534729],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-63-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "1996-12-22T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 63,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T14:18:17.483Z",
                            "updatedAt": "2023-06-14T14:18:17.483Z"
                        }
                    },
                    {
                        "id": 62,
                        "email": "1671722521890@gmail.com",
                        "phone": "+2347812345175",
                        "first_name": "Idon",
                        "last_name": "Tire",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[3.4064478874206543,6.4654221534729],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-62-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "1982-12-22T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 62,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T14:18:12.483Z",
                            "updatedAt": "2023-06-14T14:18:12.483Z"
                        }
                    },
                    {
                        "id": 60,
                        "email": "jesudarabeneficiary@gmail.com",
                        "phone": "+2347040247157",
                        "first_name": "Jesu",
                        "last_name": "Dara",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"country\":\"Nigeria\",\"state\":\"Federal Capital Territory\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-jesudarabeneficiary%40gmail.com-i.jpeg",
                        "iris": null,
                        "status": "pending",
                        "dob": "1998-12-22T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 60,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T14:18:07.483Z",
                            "updatedAt": "2023-06-14T14:18:07.483Z"
                        }
                    },
                    {
                        "id": 61,
                        "email": "1671624718876@gmail.com",
                        "phone": "+2348712345167",
                        "first_name": "Jesu",
                        "last_name": "Dara",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[7.457282066345215,9.070042610168457],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-61-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2010-12-21T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 61,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T14:18:02.482Z",
                            "updatedAt": "2023-06-14T14:18:02.482Z"
                        }
                    },
                    {
                        "id": 52,
                        "email": "charlie4biz@gmail.com",
                        "phone": "+2349031559478",
                        "first_name": "Charles",
                        "last_name": "Okaformbah",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"country\":\"Nigeria\",\"state\":\"Lagos\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-charlie4biz%40gmail.com-i.jpeg",
                        "iris": null,
                        "status": "pending",
                        "dob": "1985-11-07T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 52,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T14:17:57.483Z",
                            "updatedAt": "2023-06-14T14:17:57.483Z"
                        }
                    },
                    {
                        "id": 463,
                        "email": "1685391286813@gmail.com",
                        "phone": "2347011840621",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "female",
                        "marital_status": null,
                        "location": "{\"coordinates\":[3.4064478874206543,6.4654221534729],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-463-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-03-22T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 463,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:53:26.461Z",
                            "updatedAt": "2023-06-14T12:53:26.461Z"
                        }
                    },
                    {
                        "id": 441,
                        "email": "1683710418871@gmail.com",
                        "phone": "2347011840657",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[3.4064478874206543,6.4654221534729],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-441-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-03-06T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 441,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:53:21.461Z",
                            "updatedAt": "2023-06-14T12:53:21.461Z"
                        }
                    },
                    {
                        "id": 365,
                        "email": "1681257410251@gmail.com",
                        "phone": "2347011840621",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.953548431396484,4.792701721191406],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-365-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-06T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 365,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:53:16.461Z",
                            "updatedAt": "2023-06-14T12:53:16.461Z"
                        }
                    },
                    {
                        "id": 368,
                        "email": "1681259008049@gmail.com",
                        "phone": "2347011840621",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.953635215759277,4.793153285980225],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-368-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 368,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:52:41.461Z",
                            "updatedAt": "2023-06-14T12:52:41.461Z"
                        }
                    },
                    {
                        "id": 374,
                        "email": "1681261580111@gmail.com",
                        "phone": "2348135569178",
                        "first_name": "John",
                        "last_name": "Die",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.955639362335205,4.789984703063965],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-374-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 374,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:53:11.461Z",
                            "updatedAt": "2023-06-14T12:53:11.461Z"
                        }
                    },
                    {
                        "id": 373,
                        "email": "1681261280144@gmail.com",
                        "phone": "2347011840621",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.955639362335205,4.789984703063965],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-373-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 373,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:53:06.461Z",
                            "updatedAt": "2023-06-14T12:53:06.461Z"
                        }
                    },
                    {
                        "id": 372,
                        "email": "1681260917739@gmail.com",
                        "phone": "2348135547866",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.953608512878418,4.793158531188965],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-372-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 372,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:53:01.461Z",
                            "updatedAt": "2023-06-14T12:53:01.461Z"
                        }
                    },
                    {
                        "id": 371,
                        "email": "1681260528157@gmail.com",
                        "phone": "2347011840624",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.9538068771362305,4.793194770812988],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-371-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 371,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:52:56.461Z",
                            "updatedAt": "2023-06-14T12:52:56.461Z"
                        }
                    },
                    {
                        "id": 370,
                        "email": "1681259562116@gmail.com",
                        "phone": "2347041846098",
                        "first_name": "John",
                        "last_name": "For",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.954064846038818,4.793344974517822],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-370-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 370,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:52:51.461Z",
                            "updatedAt": "2023-06-14T12:52:51.461Z"
                        }
                    },
                    {
                        "id": 474,
                        "email": "inimdrew2@gmail.org",
                        "phone": "08445896671",
                        "first_name": "Inim",
                        "last_name": "Andrew",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{ \"country\": \"Nigeria\", \"coordinates\": [3.234222,23.234444]}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-474-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "1996-03-02T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 474,
                            "CampaignId": 284,
                            "approved": true,
                            "rejected": false,
                            "source": "field app",
                            "createdAt": "2023-05-31T12:55:35.655Z",
                            "updatedAt": "2023-05-31T12:55:35.655Z"
                        }
                    },
                    {
                        "id": 366,
                        "email": "1681258042935@gmail.com",
                        "phone": "2347011840621",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.953936576843262,4.793299198150635],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-366-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-06T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 366,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:52:31.462Z",
                            "updatedAt": "2023-06-14T12:52:31.462Z"
                        }
                    },
                    {
                        "id": 367,
                        "email": "1681258734209@gmail.com",
                        "phone": "2347011840621",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[6.953635215759277,4.793153285980225],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.us-east-2.amazonaws.com/u-p-367-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-04-01T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 367,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T12:52:36.463Z",
                            "updatedAt": "2023-06-14T12:52:36.463Z"
                        }
                    },
                    {
                        "id": 56,
                        "email": "jesudara.j@testing.com",
                        "phone": "+2347012345578",
                        "first_name": "Jesu",
                        "last_name": "Dara",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"country\":\"Nigeria\",\"state\":\"Federal Capital Territory\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-jesudara.j%40testing.com-i.jpeg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2002-12-09T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 56,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T14:18:27.482Z",
                            "updatedAt": "2023-06-14T14:18:27.482Z"
                        }
                    },
                    {
                        "id": 75,
                        "email": "1673861917729@gmail.com",
                        "phone": "+2347011840629",
                        "first_name": "Tule",
                        "last_name": "Simon",
                        "gender": "male",
                        "marital_status": null,
                        "location": "{\"coordinates\":[3.4064478874206543,6.4654221534729],\"country\":\"Nigeria\"}",
                        "tfa_binded_date": null,
                        "profile_pic": "https://convexity-profile-images.s3.amazonaws.com/u-p-75-i.jpg",
                        "iris": null,
                        "status": "pending",
                        "dob": "2023-01-02T00:00:00.000Z",
                        "Beneficiary": {
                            "UserId": 75,
                            "CampaignId": 284,
                            "approved": false,
                            "rejected": false,
                            "source": null,
                            "createdAt": "2023-06-14T14:18:22.484Z",
                            "updatedAt": "2023-06-14T14:18:22.484Z"
                        }
                    }
                ],
                "ck8": "@aHZQJiFGuKtmziTs60h8C^",
                "beneficiaries_count": 21,
                "task_count": 0,
                "completed_task": 0
            }
            ],
        "totalPages": 13,
        "currentPage": 0
    }
}"""

const val dummyResCampaignForm = """{
    "code": 200,
    "status": "success",
    "message": "Campaign form received",
    "data":{
    "totalItems":2,
    "data": [
        {
            "id": 4,
            "beneficiaryId": null,
            "organisationId": 28,
            "title": "Title of the form",
            "questions": [
                {
                    "type": "multiple",
                    "value": "",
                    "required": false,
                    "question": {
                        "title": "question 1",
                        "options": [
                            {
                                "option": "How many children do you have?",
                                "reward": 2000
                            },
                            {
                                "option": "How old are you?",
                                "reward": 1500
                            }
                        ]
                    }
                },
                {
                    "type": "optional",
                    "value": "",
                    "required": false,
                    "question": {
                        "title": "question 2",
                        "options": [
                            {
                                "option": "Date of birth",
                                "reward": 2300
                            },
                            {
                                "option": "married?",
                                "reward": 1000
                            }
                        ]
                    }
                },
                {
                    "type": "short",
                    "value": 3000,
                    "required": false,
                    "question": {
                        "title": "question 3",
                        "options": []
                    }
                }
            ],
            "createdAt": "2023-02-17T15:06:13.512Z",
            "updatedAt": "2023-02-22T22:44:29.522Z",
            "campaigns": [
                {
                    "id": 119,
                    "OrganisationId": 28,
                    "formId": 4,
                    "title": "Form test campaign",
                    "minting_limit": 0,
                    "is_processing": true,
                    "type": "campaign",
                    "spending": "vendor",
                    "collection_hash": null,
                    "escrow_hash": null,
                    "description": "form testing campaign",
                    "status": "ended",
                    "is_funded": false,
                    "is_public": true,
                    "funded_with": null,
                    "budget": 3000,
                    "contractIndex": null,
                    "amount_disbursed": 0,
                    "location": null,
                    "start_date": "2023-02-23T00:00:00.000Z",
                    "paused_date": null,
                    "end_date": "2023-03-02T00:00:00.000Z",
                    "createdAt": "2023-02-17T15:14:03.563Z",
                    "updatedAt": "2023-03-13T16:08:07.265Z"
                },
                {
                    "id": 284,
                    "OrganisationId": 28,
                    "formId": 4,
                    "title": "ArchyScript test 1",
                    "minting_limit": 0,
                    "is_processing": true,
                    "type": "campaign",
                    "spending": "vendor",
                    "collection_hash": null,
                    "escrow_hash": null,
                    "description": "grytyt ArchyScript test 1",
                    "status": "ended",
                    "is_funded": false,
                    "is_public": true,
                    "funded_with": null,
                    "budget": 45000,
                    "contractIndex": null,
                    "amount_disbursed": 0,
                    "location": null,
                    "start_date": "2023-03-14T00:00:00.000Z",
                    "paused_date": null,
                    "end_date": "2023-03-31T00:00:00.000Z",
                    "createdAt": "2023-03-13T16:14:31.296Z",
                    "updatedAt": "2023-04-06T10:57:58.303Z"
                }
            ]
        }
    ]}
}"""
