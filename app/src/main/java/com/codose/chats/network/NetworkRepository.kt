package com.codose.chats.network

import android.content.Context
import com.codose.chats.model.ModelCampaign
import com.codose.chats.model.NFCModel
import com.codose.chats.network.api.ConvexityApiService
import com.codose.chats.network.api.RetrofitClient
import com.codose.chats.network.api.SessionManager
import com.codose.chats.network.body.VendorBody
import com.codose.chats.network.body.login.LoginBody
import com.codose.chats.network.response.BaseResponse
import com.codose.chats.network.response.NfcUpdateResponse
import com.codose.chats.network.response.RegisterResponse
import com.codose.chats.network.response.UserDetailsResponse
import com.codose.chats.network.response.campaign.CampaignByOrganizationModel
import com.codose.chats.network.response.forgot.ForgotBody
import com.codose.chats.network.response.forgot.ForgotPasswordResponse
import com.codose.chats.network.response.login.LoginResponse
import com.codose.chats.network.response.organization.OrganizationResponse
import com.codose.chats.network.response.organization.campaign.CampaignResponse
import com.codose.chats.network.response.progress.PostCompletionBody
import com.codose.chats.network.response.progress.SubmitProgressModel
import com.codose.chats.network.response.tasks.GetTasksModel
import com.codose.chats.network.response.tasks.details.TaskDetailsModel
import com.codose.chats.offline.OfflineRepository
import com.codose.chats.utils.ApiResponse
import com.codose.chats.utils.PrefUtils
import com.codose.chats.utils.Utils
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import timber.log.Timber
import java.io.File
import java.lang.Exception

class NetworkRepository(
    private val api: ConvexityApiService,
    private val offlineRepository: OfflineRepository,
    private val context: Context
) {

    private val sessionManager: SessionManager = SessionManager(context)
    suspend fun onboardUser(
        organizationId: String,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        phone: RequestBody,
        password: RequestBody,
        lat: RequestBody,
        long: RequestBody,
        nfc: RequestBody,
        status: RequestBody,
        profile_pic: File,
        prints: ArrayList<MultipartBody.Part>,
        mGender: RequestBody,
        mDate: RequestBody,
        location: RequestBody,
        campaign: RequestBody,
        pin: RequestBody,
    ) : ApiResponse<RegisterResponse>{
        return try {
            val compressed = Compressor.compress(context, profile_pic)

            val mBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), compressed)

            val image = MultipartBody.Part.createFormData(
                "profile_pic",
                compressed.absolutePath.substringAfterLast("/"),
                mBody
            )
            val data = api.onboardUser(
                organizationId,
                firstName,
                lastName,
                email,
                phone,
                password,
                lat,
                long,
                location,
                nfc,
                status,
                image,
                prints,
                mGender,
                mDate,
                campaign,
                pin,
            ).await()
            ApiResponse.Success(data)
        }catch (e : HttpException){
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun onboardSpecialUser(
        organizationId: String,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        phone: RequestBody,
        password: RequestBody,
        lat: RequestBody,
        long: RequestBody,
        nfc: RequestBody,
        status: RequestBody,
        profile_pic: File,
        mGender: RequestBody,
        mDate: RequestBody,
        location: RequestBody,
        campaign: RequestBody,
        pin: RequestBody,
        nin: RequestBody?,
    ) : ApiResponse<RegisterResponse>{
        return try {
            val compressed = Compressor.compress(context, profile_pic)

            val mBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), compressed)

            val image = MultipartBody.Part.createFormData(
                "profile_pic",
                compressed.absolutePath.substringAfterLast("/"),
                mBody
            )
            val data = api.onboardSpecialUser(
                organizationId,
                firstName,
                lastName,
                email,
                phone,
                password,
                lat,
                long,
                location,
                nfc,
                status,
                nin,
                image,
                mGender,
                mDate,
                campaign,
                pin,
            ).await()
            ApiResponse.Success(data)
        }catch (e : HttpException){
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun vendorOnboarding(
        businessName: String,
        email: String,
        phone: String,
        password: String,
        pin: String,
        bvn: String,
        firstName: String,
        lastName: String,
        address: String,
        country: String,
        state: String
    ) : ApiResponse<RegisterResponse>{
        return try {
            val requestBody = VendorBody(bvn,email,businessName,password,phone,pin,businessName, firstName, lastName,
                address = address, country = country, state = state)
            val data = api.vendorOnboarding(requestBody).await()
            ApiResponse.Success(data)
        }catch (e : HttpException){
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getNGOs() : ApiResponse<OrganizationResponse>{
        return try {
            val data = api.getNGOs().await()
            ApiResponse.Success(data)
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getUserDetails(id : String) : ApiResponse<UserDetailsResponse>{
        return try {
            val data = api.getUserDetails(id).await()
            ApiResponse.Success(data)
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun loginNGO(loginBody: LoginBody) : ApiResponse<LoginResponse>{
        return try {
            val data = api.loginNGO(loginBody).await()
            PrefUtils.setNGOToken("Bearer "+data.data.token)
            getCampaigns()
            ApiResponse.Success(data)
        }catch (e : HttpException){
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun sendForgotEmail(email: String) : ApiResponse<ForgotPasswordResponse>{
        return try {
            val forgotBody = ForgotBody(email)
            val data = api.sendForgotMail(forgotBody).await()
            ApiResponse.Success(data)
        }catch (e : HttpException){
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun postNFCDetails(nfcModel: NFCModel): ApiResponse<NfcUpdateResponse> {
        return try {
            val data = api.postNfcDetails(nfcModel).await()
            ApiResponse.Success(data)
        }catch (e : HttpException){
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        }catch (t : Throwable){
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getCampaigns(): ApiResponse<CampaignResponse> {
        return try {
            val data = api.getCampaigns().await()
            Timber.v("Campaign: $data")
            offlineRepository.insertCampaign(data.data)
            ApiResponse.Success(data)
        } catch (e : HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t : Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getAllCampaigns(): List<ModelCampaign>{
        return try{
            val apiService =  RetrofitClient.apiService().create(ConvexityApiService::class.java)
            var data = apiService.getAllCampaigns(PrefUtils.getNGOId(), "campaign").data
            Timber.v("XXXgetAllCampaigns: called"+data.toString())
            offlineRepository.insertAllCampaign(data)
            data
        }
        catch (e: Exception){
            Timber.v("XXXgetAllCampaigns"+e.message)
            var data: List<ModelCampaign> = ArrayList()
            data
        }
    }

    suspend fun getAllCashForWorkCampaigns(): List<ModelCampaign> {
        return try {
            withContext(Dispatchers.IO) {
                val data = api.getAllCampaigns(PrefUtils.getNGOId(), "cash-for-work").data
                Timber.v("XXXgetAllCampaigns: called$data")
                offlineRepository.insertAllCashForWork(data)
                data
            }
        } catch (e: Exception) {
            Timber.e(e)
            val data: List<ModelCampaign> = ArrayList()
            data
        }
    }

    suspend fun getCampaignByOrganization(organizationId : String): ApiResponse<CampaignByOrganizationModel> {
        return try {
            val data = api.getCampaignsByOrganization(organizationId).await()
            ApiResponse.Success(data)
        } catch (e : HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t : Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getTasks(campaignId : String): ApiResponse<GetTasksModel> {
        return try {
            val data = api.getTasks(campaignId).await()
            ApiResponse.Success(data)
        } catch (e : HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t : Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getTasksDetails(taskId: String): BaseResponse<TaskDetailsModel> {
        return withContext(Dispatchers.IO) { api.getTasksDetails(taskId) }
    }

    suspend fun postTaskEvidence(
        taskId : String,
        userId : String,
        description : String,
        images :  ArrayList<File>,
    ): ApiResponse<SubmitProgressModel> {
        return try {
            val taskIdBody = taskId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val userIdBody = userId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val descriptionBody =
                description.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val imageParts = ArrayList<MultipartBody.Part>()
            images.forEachIndexed { index, image ->
                val compressed = Compressor.compress(context, image)
                val mBody = compressed.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData(
                    "images_$index",
                    compressed.absolutePath.substringAfterLast("/"),
                    mBody
                )
                imageParts.add(imagePart)
            }
            val data =
                api.postTaskEvidence(taskIdBody, userIdBody, descriptionBody, imageParts).await()
            ApiResponse.Success(data)
        } catch (e : HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t : Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun postTaskCompleted(
        taskId: String,
        userId: String,
    ): ApiResponse<SubmitProgressModel> {
        return try {
            val postCompletionBody = PostCompletionBody(taskId = taskId.toInt(), userId = userId.toInt())
            val data = api.postTaskCompleted(postCompletionBody).await()
            ApiResponse.Success(data)
        } catch (e : HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t : Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun addBeneficiaryToCampaign(beneficiaryId: Int, campaignId: Int) =
        withContext(Dispatchers.IO) {
            api.addBeneficiaryToCampaign(beneficiaryId, campaignId)
        }

    suspend fun getBeneficiaryByOrganisation() =
        withContext(Dispatchers.IO) {
            api.getBeneficiariesByOrganisation()
        }
}
