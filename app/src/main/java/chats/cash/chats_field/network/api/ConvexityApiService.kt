package chats.cash.chats_field.network.api

import chats.cash.chats_field.model.NFCModel
import chats.cash.chats_field.model.campaignform.AllCampaignFormResponse
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryGroupDetails
import chats.cash.chats_field.network.body.impact_report.ImpactReportBody
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.network.response.BaseResponse
import chats.cash.chats_field.network.response.NfcUpdateResponse
import chats.cash.chats_field.network.response.RegisterResponse
import chats.cash.chats_field.network.response.VendorDetailsResponse
import chats.cash.chats_field.network.response.beneficiary_onboarding.OrganizationBeneficiary
import chats.cash.chats_field.network.response.beneficiary_onboarding.UploadProfilePicResponse
import chats.cash.chats_field.network.response.campaign.CampaignByOrganizationModel
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.campaign.GetAllCampaignsResponse
import chats.cash.chats_field.network.response.forgot.ForgotBody
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.group_beneficiary.OnboardGroupBeneficiaryResponse
import chats.cash.chats_field.network.response.impact_report.ImpactReportResponse
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.organization.campaign.CampaignResponse
import chats.cash.chats_field.network.response.progress.PostCompletionBody
import chats.cash.chats_field.network.response.progress.SubmitProgressModel
import chats.cash.chats_field.network.response.tasks.GetTasksModel
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.views.beneficiary_onboarding.model.AddBeneficiaryResponse
import chats.cash.chats_field.views.cashForWork.model.TaskDetailsResponse
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
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
        @Header("Authorization") authorization: String,
    ): Deferred<RegisterResponse>

    @Multipart
    @POST("ngos/{organisation_id}/beneficiaries")
    suspend fun onboardBeneficiary(
        @Path("organisation_id") organisationId: String,
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("password") password: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") long: RequestBody,
        @Part("location") location: RequestBody,
        @Part("nfc") nfc: RequestBody,
        @Part("iris") iris: RequestBody? = null,
        @Part("role") status: RequestBody,
        @Part profile_pic: MultipartBody.Part,
        @Part prints: ArrayList<MultipartBody.Part>,
        @Part("gender") gender: RequestBody,
        @Part("dob") date: RequestBody,
        @Part("campaign") campaign: RequestBody,
        //   @Part("pin") pin: RequestBody,
        @Header("Authorization") authorization: String,
    ): RegisterResponse

    @Multipart
    @Deprecated("")
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
        @Header("Authorization") authorization: String,
    ): Deferred<RegisterResponse>

    @Multipart
    @POST("ngos/{organisation_id}/beneficiaries/special-case")
    suspend fun onboardSpecialBeneficiary(
        @Path("organisation_id") organisationId: String,
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("password") password: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") long: RequestBody,
        @Part("iris") iris: RequestBody? = null,
        @Part("location") location: RequestBody,
        @Part("nfc") nfc: RequestBody,
        @Part("role") status: RequestBody,
        @Part("nin") nin: RequestBody?,
        @Part profile_pic: MultipartBody.Part,
        @Part("gender") gender: RequestBody,
        @Part("dob") date: RequestBody,
        @Part("campaign") campaign: RequestBody,
        // @Part("pin") pin: RequestBody,
        @Header("Authorization") authorization: String,
    ): RegisterResponse

    @Multipart
    @POST("organisations/group-beneficiaries")
    suspend fun onboardGroupBeneficiary(
        @Part("group") group: GroupBeneficiaryGroupDetails,
        @Part members: List<MultipartBody.Part>,
        @Part representative: MultipartBody.Part,
        @Header("Authorization") authorization: String,
    ): OnboardGroupBeneficiaryResponse

    @POST("organisations/group-beneficiaries")
    @JvmSuppressWildcards
    suspend fun onboardGroupBeneficiary(
        @Body body: GroupBeneficiaryBody,
        @Header("Authorization") authorization: String,
    ): OnboardGroupBeneficiaryResponse

    @POST("organisations/upload-profile-pic")
    @Multipart
    suspend fun UploadImage(
        @Part profile_pic: MultipartBody.Part,
        @Header("Authorization") authorization: String,
    ): UploadProfilePicResponse

    @POST("organisations/{organisation_id}/vendors")
    @FormUrlEncoded
    @Deprecated(
        "this is wrong, as it resturns Any instead of returning ",
        replaceWith = ReplaceWith("VendorOnboarding2"),
    )
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
        @Header("Authorization") authorization: String,
    ): BaseResponse<Any>

    @POST("organisations/{organisation_id}/vendors")
    @FormUrlEncoded
    suspend fun vendorOnboarding2(
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
        @Header("Authorization") authorization: String,
    ): VendorOnboardingResponse

    @POST("vendors/auth/{userId}")
    suspend fun getVendorDetails(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
    ): VendorDetailsResponse

//    @GET("ngos")
//    fun getNGOs(): Deferred<OrganizationResponse>

    @POST("auth/field-login")
    suspend fun loginNGO(@Body loginBody: LoginBody): LoginResponse

    @POST("users/reset-password")
    suspend fun sendForgotMail(@Body forgotBody: ForgotBody): ForgotPasswordResponse

    @PUT("users/nfc_update")
    suspend fun postNfcDetails(
        @Body nfcModel: NFCModel,
        @Header("Authorization") authorization: String,
    ): NfcUpdateResponse

    @GET("campaigns/all/")
    suspend fun getCampaigns(@Header("Authorization") authorization: String): CampaignResponse

    @GET("campaigns/organisation/{id}")
    fun getCampaignsByOrganization(
        @Path("id") id: String,
        @Query("type") type: String = "cash-for-work",
        @Header("Authorization") authorization: String,
    ): CampaignByOrganizationModel

    @GET("cash-for-work/tasks/{id}")
    fun getTasks(
        @Path("id") campaignId: String,
        @Header("Authorization") authorization: String,
    ): Deferred<GetTasksModel>

    /*@GET("cash-for-work/task/{taskId}")
    suspend fun getTasksDetails(
        @Path("taskId") taskId: String,
        @Header("Authorization") authorization: String = PrefUtils.getNGOToken()
    ): BaseResponse<TaskDetailsModel>*/

    @GET("tasks/cash-for-work/task/{taskId}")
    suspend fun getTasksDetails(
        @Path("taskId") taskId: String,
        @Header("Authorization") authorization: String,
    ): BaseResponse<TaskDetailsResponse>

    @POST("cash-for-work/task/submit-progress")
    @Multipart
    fun postTaskEvidence(
        @Part("taskId") taskId: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("description") description: RequestBody,
        @Part images: ArrayList<MultipartBody.Part>,
        @Header("Authorization") authorization: String,
    ): Deferred<SubmitProgressModel>

    @POST("cash-for-work/task/agent-evidence/{beneficiaryId}")
    @Multipart
    suspend fun uploadTaskEvidence(
        @Path("beneficiaryId") beneficiaryId: Int,
        @Part("TaskAssignmentId") taskAssignmentId: RequestBody,
        @Part("comment") description: RequestBody,
        @Part("location") location: RequestBody,
        @Part("type") type: RequestBody,
        @Part uploads: ArrayList<MultipartBody.Part>,
        @Header("Authorization") authorization: String,
    ): BaseResponse<Any>

    @POST("cash-for-work/task/progress/confirm")
    fun postTaskCompleted(@Body postCompletionBody: PostCompletionBody): Deferred<SubmitProgressModel>

    @GET("organisations/{id}/campaigns/all")
    suspend fun getAllCampaigns(
        @Path("id") id: Int,
        @Query("type") type: String?,
        @Header("Authorization") authorization: String,
    ): GetAllCampaignsResponse

    @GET("beneficiaries/survey/{campaign_id}")
    suspend fun getCampaignSurvey(
        @Path("campaign_id") campaignId: Int,
        @Header("Authorization") authorization: String,
    ): Response<CampaignSurveyResponse>

    @GET("beneficiaries/survey/{campaign_id}")
    suspend fun getCampaignSurvey2(
        @Path("campaign_id") campaignId: Int,
        @Header("Authorization") authorization: String,
    ): CampaignSurveyResponse

    @GET("organisations/{organisation_id}/campaign_form")
    suspend fun getAllCampaignForms(
        @Path("organisation_id") organisationId: Int,
        @Header("Authorization") authorization: String,
    ): Response<AllCampaignFormResponse>

    @GET("organisations/{organisation_id}/campaign_form")
    suspend fun getAllCampaignForms2(
        @Path("organisation_id") organisationId: Int,
        @Header("Authorization") authorization: String,
    ): AllCampaignFormResponse

    @GET("organisation/{organisation_id}/beneficiaries")
    suspend fun getBeneficiariesByOrganisation(
        @Path("organisation_id") organisationId: Int,
        @Header("Authorization") authorization: String,
    ): BaseResponse<List<OrganizationBeneficiary>>

    @GET("organisations/non-org-beneficiary")
    suspend fun getExistingBeneficiary(
        @Query("first_name") firstName: String? = null,
        @Query("last_name") lastName: String? = null,
        @Query("email") email: String? = null,
        @Query("nin") nin: String? = null,
        @Query("phone") phone: String? = null,
        @Header("Authorization") authorization: String,
    ): BaseResponse<List<OrganizationBeneficiary>>

    @POST("beneficiaries/{beneficiary_id}/campaigns/{campaign_id}/join")
    suspend fun addBeneficiaryToCampaign(
        @Path("beneficiary_id") beneficiaryId: Int,
        @Path("campaign_id") campaignId: Int,
        @Header("Authorization") authorization: String,
    ): BaseResponse<AddBeneficiaryResponse>

    @GET("organisations/{organisation_id}/campaigns/{campaign_id}/beneficiaries")
    suspend fun getBeneficiariesByCampaign(
        @Path("organisation_id") organisationId: Int,
        @Path("campaign_id") campaignId: Int,
        @Header("Authorization") authorization: String,
    ): BaseResponse<List<OrganizationBeneficiary>>

    @POST("beneficiaries/field/survey/{campaign_id}")
    suspend fun answerCampaignSurvey(
        @Body answer: SubmitSurveyAnswerBody,
        @Path("campaign_id") campaignId: Int,
        @Header("Authorization") authorization: String,
    )

    @POST("impact-reports/create-report")
    suspend fun submitImpactReport(
        @Body answer: ImpactReportBody,
        @Header("Authorization") authorization: String,
    ): ImpactReportResponse
}
