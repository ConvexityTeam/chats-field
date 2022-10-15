package chats.cash.chats_field.network.api

import chats.cash.chats_field.model.NFCModel
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.response.BaseResponse
import chats.cash.chats_field.network.response.NfcUpdateResponse
import chats.cash.chats_field.network.response.RegisterResponse
import chats.cash.chats_field.network.response.UserDetailsResponse
import chats.cash.chats_field.network.response.beneficiary_onboarding.Beneficiary
import chats.cash.chats_field.network.response.campaign.CampaignByOrganizationModel
import chats.cash.chats_field.network.response.campaign.GetAllCampaignsResponse
import chats.cash.chats_field.network.response.forgot.ForgotBody
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.login.Data
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.organization.OrganizationResponse
import chats.cash.chats_field.network.response.organization.campaign.CampaignResponse
import chats.cash.chats_field.network.response.progress.PostCompletionBody
import chats.cash.chats_field.network.response.progress.SubmitProgressModel
import chats.cash.chats_field.network.response.tasks.GetTasksModel
import chats.cash.chats_field.views.beneficiary_onboarding.model.AddBeneficiaryResponse
import chats.cash.chats_field.views.cashForWork.model.TaskDetailsResponse
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import kotlin.collections.ArrayList

interface ConvexityApiService {

    @Multipart
    @POST("ngos/{organisation_id}/beneficiaries")
    fun onboardUser(
        @Path("organisation_id") id: String,
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("password") password: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") long: RequestBody,
        @Part("location") location: RequestBody,
        @Part("nfc") nfc: RequestBody,
        @Part("role") status: RequestBody,
        @Part profile_pic: MultipartBody.Part,
        @Part prints: ArrayList<MultipartBody.Part>,
        @Part("gender") gender: RequestBody,
        @Part("dob") date: RequestBody,
        @Part("campaign") campaign: RequestBody,
        @Part("pin") pin: RequestBody,
        @Header("Authorization") authorization: String
    ): Deferred<RegisterResponse>

    @Multipart
    @POST("ngos/{organisation_id}/beneficiaries/special-case")
    fun onboardSpecialUser(
        @Path("organisation_id") id: String,
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("password") password: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") long: RequestBody,
        @Part("location") location: RequestBody,
        @Part("nfc") nfc: RequestBody,
        @Part("role") status: RequestBody,
        @Part("nin") nin: RequestBody?,
        @Part profile_pic: MultipartBody.Part,
        @Part("gender") gender: RequestBody,
        @Part("dob") date: RequestBody,
        @Part("campaign") campaign: RequestBody,
        @Part("pin") pin: RequestBody,
        @Header("Authorization") authorization: String
    ): Deferred<RegisterResponse>

    @POST("organisations/{organisation_id}/vendors")
    @FormUrlEncoded
    suspend fun vendorOnboarding(
        @Path("organisation_id") organisationId: Int,
        @Field("first_name") firstName: String,
        @Field("last_name") lastName: String,
        @Field("email") email: String,
        @Field("store_name") storeName: String,
        @Field("country") country: String,
        @Field("address") address: String,
        @Field("phone") phone: String,
        @Field("state") state: String,
        @Field("location") location: String,
        @Header("Authorization") authorization: String
    ): BaseResponse<Any>

    @POST("vendors/auth/{userId}")
    fun getUserDetails(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String
    ): Deferred<UserDetailsResponse>

    @GET("ngos")
    fun getNGOs(): Deferred<OrganizationResponse>

    @Deprecated(level = DeprecationLevel.WARNING, message = "Replaced with Coroutine-supported methods")
    @POST("auth/login")
    fun loginNGO(@Body loginBody: LoginBody): Deferred<LoginResponse>

    @POST("auth/login")
    suspend fun loginNgo(@Body loginBody: LoginBody): BaseResponse<Data>

    @POST("users/reset-password")
    fun sendForgotMail(@Body forgotBody: ForgotBody): Deferred<ForgotPasswordResponse>

    @PUT("users/nfc_update")
    fun postNfcDetails(
        @Body nfcModel: NFCModel,
        @Header("Authorization") authorization: String
    ): Deferred<NfcUpdateResponse>

    @GET("campaigns/all/")
    fun getCampaigns(@Header("Authorization") authorization: String): Deferred<CampaignResponse>

    @GET("campaigns/organisation/{id}")
    fun getCampaignsByOrganization(
        @Path("id") id: String,
        @Query("type") type: String = "cash-for-work",
        @Header("Authorization") authorization: String
    ): Deferred<CampaignByOrganizationModel>

    @GET("cash-for-work/tasks/{id}")
    fun getTasks(
        @Path("id") campaignId: String,
        @Header("Authorization") authorization: String
    ): Deferred<GetTasksModel>

    /*@GET("cash-for-work/task/{taskId}")
    suspend fun getTasksDetails(
        @Path("taskId") taskId: String,
        @Header("Authorization") authorization: String = PrefUtils.getNGOToken()
    ): BaseResponse<TaskDetailsModel>*/

    @GET("tasks/cash-for-work/task/{taskId}")
    suspend fun getTasksDetails(
        @Path("taskId") taskId: String,
        @Header("Authorization") authorization: String
    ): BaseResponse<TaskDetailsResponse>

    @POST("cash-for-work/task/submit-progress")
    @Multipart
    fun postTaskEvidence(
        @Part("taskId") taskId: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("description") description: RequestBody,
        @Part images: ArrayList<MultipartBody.Part>,
        @Header("Authorization") authorization: String
    ): Deferred<SubmitProgressModel>

    @POST("cash-for-work/task/agent-evidence/{beneficiaryId}")
    @Multipart
    suspend fun uploadTaskEvidence(
        @Path("beneficiaryId") beneficiaryId: Int,
        @Part("TaskAssignmentId") taskAssignmentId: RequestBody,
        @Part("comment") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part uploads: ArrayList<MultipartBody.Part>,
        @Header("Authorization") authorization: String
    ): BaseResponse<Any>

    @POST("cash-for-work/task/progress/confirm")
    fun postTaskCompleted(@Body postCompletionBody: PostCompletionBody): Deferred<SubmitProgressModel>

    @GET("organisations/{id}/campaigns/all")
    suspend fun getAllCampaigns(
        @Path("id") id: Int,
        @Query("type") type: String?,
        @Header("Authorization") authorization: String
    ): GetAllCampaignsResponse

    @GET("organisation/{organisation_id}/beneficiaries")
    suspend fun getBeneficiariesByOrganisation(
        @Path("organisation_id") organisationId: Int,
        @Header("Authorization") authorization: String
    ): BaseResponse<List<Beneficiary>>

    @GET("organisations/non-org-beneficiary")
    suspend fun getExistingBeneficiary(
        @Query("first_name") firstName: String? = null,
        @Query("last_name") lastName: String? = null,
        @Query("email") email: String? = null,
        @Query("nin") nin: String? = null,
        @Query("phone") phone: String? = null,
        @Header("Authorization") authorization: String
    ): BaseResponse<List<Beneficiary>>

    @POST("beneficiaries/{beneficiary_id}/campaigns/{campaign_id}/join")
    suspend fun addBeneficiaryToCampaign(
        @Path("beneficiary_id") beneficiaryId: Int,
        @Path("campaign_id") campaignId: Int,
        @Header("Authorization") authorization: String
    ): BaseResponse<AddBeneficiaryResponse>

    @GET("organisations/{organisation_id}/campaigns/{campaign_id}/beneficiaries")
    suspend fun getBeneficiariesByCampaign(
        @Path("organisation_id") organisationId: Int,
        @Path("campaign_id") campaignId: Int,
        @Header("Authorization") authorization: String
    ): BaseResponse<List<Beneficiary>>
}
