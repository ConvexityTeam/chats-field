package com.codose.chats.network.api

import com.codose.chats.model.NFCModel
import com.codose.chats.network.body.VendorBody
import com.codose.chats.network.body.login.LoginBody
import com.codose.chats.network.response.NfcUpdateResponse
import com.codose.chats.network.response.RegisterResponse
import com.codose.chats.network.response.UserDetailsResponse
import com.codose.chats.network.response.campaign.CampaignByOrganizationModel
import com.codose.chats.network.response.campaign.GetAllCampaignsResponse
import com.codose.chats.network.response.forgot.ForgotBody
import com.codose.chats.network.response.forgot.ForgotPasswordResponse
import com.codose.chats.network.response.login.LoginResponse
import com.codose.chats.network.response.organization.OrganizationResponse
import com.codose.chats.network.response.organization.campaign.CampaignResponse
import com.codose.chats.network.response.progress.PostCompletionBody
import com.codose.chats.network.response.progress.SubmitProgressModel
import com.codose.chats.network.response.tasks.GetTasksModel
import com.codose.chats.network.response.tasks.details.TaskDetailsModel
import com.codose.chats.utils.PrefUtils
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*
import kotlin.collections.ArrayList

interface ConvexityApiService {

    @Multipart
    @POST("auth/register")
    fun onboardUser(
        @Part("first_name") firstName : RequestBody,
        @Part("last_name") lastName : RequestBody,
        @Part("email") email : RequestBody,
        @Part("phone") phone : RequestBody,
        @Part("password") password : RequestBody,
        @Part("latitude") lat : RequestBody,
        @Part("longitude") long : RequestBody,
        @Part("location") location : RequestBody,
        @Part("nfc") nfc : RequestBody,
        @Part("role") status : RequestBody,
        @Part profile_pic :  MultipartBody.Part,
        @Part prints :  ArrayList<MultipartBody.Part>,
        @Part("gender") gender : RequestBody,
        @Part("dob") date : RequestBody,
        @Part("campaign") campaign : RequestBody,
        @Header("Authorization") authorization : String = PrefUtils.getNGOToken()
    ) : Deferred<RegisterResponse>

    @Multipart
    @POST("auth/register/special-case")
    fun onboardSpecialUser(
        @Part("first_name") firstName : RequestBody,
        @Part("last_name") lastName : RequestBody,
        @Part("email") email : RequestBody,
        @Part("phone") phone : RequestBody,
        @Part("password") password : RequestBody,
        @Part("latitude") lat : RequestBody,
        @Part("longitude") long : RequestBody,
        @Part("location") location : RequestBody,
        @Part("nfc") nfc : RequestBody,
        @Part("role") status : RequestBody,
        @Part("nin") nin : RequestBody?,
        @Part profile_pic :  MultipartBody.Part,
        @Part("gender") gender : RequestBody,
        @Part("dob") date : RequestBody,
        @Part("campaign") campaign : RequestBody,
        @Header("Authorization") authorization : String = PrefUtils.getNGOToken()
    ) : Deferred<RegisterResponse>

    @POST("vendors/auth/register")
    fun vendorOnboarding(
        @Body requestBody : VendorBody,
        @Header("Authorization") authorization : String = PrefUtils.getNGOToken()
    ) : Deferred<RegisterResponse>

    @POST("vendors/auth/{userId}")
    fun getUserDetails(
        @Path("userId") userId: String,
        @Header("Authorization") authorization : String = PrefUtils.getNGOToken()
    ) : Deferred<UserDetailsResponse>

    @GET("ngos")
    fun getNGOs() : Deferred<OrganizationResponse>

    @POST("auth/login")
    fun loginNGO(@Body loginBody : LoginBody) : Deferred<LoginResponse>

    @POST("users/reset-password")
    fun sendForgotMail(@Body forgotBody : ForgotBody) : Deferred<ForgotPasswordResponse>

    @PUT("users/nfc_update")
    fun postNfcDetails(@Body nfcModel: NFCModel ,
                       @Header("Authorization") authorization : String = PrefUtils.getNGOToken()) : Deferred<NfcUpdateResponse>

    @GET("campaigns/all/")
    fun getCampaigns(@Header("Authorization") authorization : String = PrefUtils.getNGOToken()) : Deferred<CampaignResponse>

    @GET("campaigns/organisation/{id}")
    fun getCampaignsByOrganization(@Path("id") id : String, @Query("type") type : String = "cash-for-work", @Header("Authorization") authorization : String = PrefUtils.getNGOToken()) : Deferred<CampaignByOrganizationModel>

    @GET("cash-for-work/tasks/{id}")
    fun getTasks(@Path("id") campaignId : String, @Header("Authorization") authorization : String = PrefUtils.getNGOToken()) : Deferred<GetTasksModel>

    @GET("cash-for-work/task/{id}")
    fun getTasksDetails(@Path("id") taskId : String, @Header("Authorization") authorization : String = PrefUtils.getNGOToken()) : Deferred<TaskDetailsModel>

    @POST("cash-for-work/task/submit-progress")
    @Multipart
    fun postTaskEvidence(
        @Part("taskId") taskId : RequestBody,
        @Part("userId") userId : RequestBody,
        @Part("description") description : RequestBody,
        @Part images :  ArrayList<MultipartBody.Part>,
        @Header("Authorization") authorization : String = PrefUtils.getNGOToken()
    ) : Deferred<SubmitProgressModel>

    @POST("cash-for-work/task/progress/confirm")
    fun postTaskCompleted(@Body postCompletionBody: PostCompletionBody) : Deferred<SubmitProgressModel>

    @GET("organisations/{id}/campaigns")
    suspend fun GetAllCampaigns(
        @Path("id") id : Int, @Query("type") type : String
    ): GetAllCampaignsResponse

    @GET("organisations/{id}/campaigns/all")
    suspend fun GetCampaigns(
        @Path("id") id : Int, @Query("type") type : String,
        @Header("Authorization") authorization : String = PrefUtils.getNGOToken()
    ): GetAllCampaignsResponse

}