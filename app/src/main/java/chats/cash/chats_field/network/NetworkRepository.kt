package chats.cash.chats_field.network

import android.content.Context
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.api.ConvexityApiService
import chats.cash.chats_field.network.api.NinVerificationApi
import chats.cash.chats_field.network.response.BaseResponse
import chats.cash.chats_field.network.response.beneficiary_onboarding.OrganizationBeneficiary
import chats.cash.chats_field.network.response.campaign.CampaignByOrganizationModel
import chats.cash.chats_field.network.response.progress.PostCompletionBody
import chats.cash.chats_field.network.response.progress.SubmitProgressModel
import chats.cash.chats_field.network.response.tasks.GetTasksModel
import chats.cash.chats_field.offline.OfflineRepository
import chats.cash.chats_field.utils.ApiResponse
import chats.cash.chats_field.utils.PreferenceUtilInterface
import chats.cash.chats_field.utils.Utils
import chats.cash.chats_field.utils.location.UserLocation
import chats.cash.chats_field.views.beneficiary_list.BeneficiaryUi
import chats.cash.chats_field.views.cashForWork.model.TaskDetailsResponse
import com.google.gson.Gson
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.File

class NetworkRepository(
    private val api: ConvexityApiService,
    private val ninApi: NinVerificationApi,
    private val offlineRepository: OfflineRepository,
    private val context: Context,
    private val preferenceUtil: PreferenceUtilInterface,
) {

//    suspend fun postNFCDetails(nfcModel: NFCModel): ApiResponse<NfcUpdateResponse> {
//        return try {
//            val data =
//                api.postNfcDetails(nfcModel, authorization = preferenceUtil.getNGOToken())
//            ApiResponse.Success(data)
//        } catch (e: HttpException) {
//            val message = Utils.getErrorMessage(e)
//            ApiResponse.Failure(message, e.code())
//        } catch (t: Throwable) {
//            ApiResponse.Failure(t.message!!)
//        }
//    }

    suspend fun getExistingBeneficiaries(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        phone: String? = null,
        nin: String? = null,
    ): BaseResponse<List<OrganizationBeneficiary>> {
        return withContext(Dispatchers.IO) {
            api.getExistingBeneficiary(
                firstName = firstName,
                lastName = lastName,
                email = email,
                nin = nin,
                phone = phone,
                authorization = preferenceUtil.getNGOToken(),
            )
        }
    }

    suspend fun getAllCashForWorkCampaigns(): List<ModelCampaign> {
        return try {
            withContext(Dispatchers.IO) {
                val data = api.getAllCampaigns(
                    preferenceUtil.getNGOId(),
                    "cash-for-work",
                    authorization = preferenceUtil.getNGOToken(),
                ).data.data
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

    suspend fun getCampaignByOrganization(organizationId: String): ApiResponse<CampaignByOrganizationModel> {
        return try {
            val data = api.getCampaignsByOrganization(
                organizationId,
                authorization = preferenceUtil.getNGOToken(),
            )
            ApiResponse.Success(data)
        } catch (e: HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t: Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getTasks(campaignId: String): ApiResponse<GetTasksModel> {
        return try {
            val data =
                api.getTasks(campaignId, authorization = preferenceUtil.getNGOToken()).await()
            ApiResponse.Success(data)
        } catch (e: HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t: Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun getTasksDetails(taskId: String): BaseResponse<TaskDetailsResponse> {
        return withContext(Dispatchers.IO) {
            api.getTasksDetails(
                taskId,
                authorization = preferenceUtil.getNGOToken(),
            )
        }
    }

    suspend fun postTaskEvidence(
        taskId: String,
        userId: String,
        description: String,
        images: ArrayList<File>,
    ): ApiResponse<SubmitProgressModel> {
        ApiResponse.Loading<SubmitProgressModel>()
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
                    mBody,
                )
                imageParts.add(imagePart)
            }
            val data =
                api.postTaskEvidence(
                    taskIdBody,
                    userIdBody,
                    descriptionBody,
                    imageParts,
                    authorization = preferenceUtil.getNGOToken(),
                ).await()
            ApiResponse.Success(data)
        } catch (e: HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t: Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun uploadTaskEvidence(
        beneficiaryId: Int,
        taskAssignmentId: String,
        location: UserLocation,
        comment: String,
        type: String = "image",
        uploads: ArrayList<File>,
    ): BaseResponse<Any> = withContext(Dispatchers.IO) {
        val typeBody = type.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val commentBody = comment.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val locationJson = Gson().toJson(location).toRequestBody(
            "multipart/form-data".toMediaTypeOrNull(),
        )
        val taskAssignmentBody =
            taskAssignmentId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val imageParts = ArrayList<MultipartBody.Part>()
        uploads.forEachIndexed { index, imageFile ->
            val mBody = imageFile.asRequestBody("image/png".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "images_$index",
                imageFile.name,
                mBody,
            )
            imageParts.add(imagePart)
        }
        api.uploadTaskEvidence(
            beneficiaryId = beneficiaryId,
            taskAssignmentId = taskAssignmentBody,
            description = commentBody,
            type = typeBody,
            location = locationJson,
            uploads = imageParts,
            authorization = preferenceUtil.getNGOToken(),
        )
    }

    suspend fun postTaskCompleted(
        taskId: String,
        userId: String,
    ): ApiResponse<SubmitProgressModel> {
        return try {
            val postCompletionBody =
                PostCompletionBody(taskId = taskId.toInt(), userId = userId.toInt())
            val data = api.postTaskCompleted(postCompletionBody).await()
            ApiResponse.Success(data)
        } catch (e: HttpException) {
            val message = Utils.getErrorMessage(e)
            ApiResponse.Failure(message, e.code())
        } catch (t: Throwable) {
            ApiResponse.Failure(t.message!!)
        }
    }

    suspend fun addBeneficiaryToCampaign(beneficiaryId: Int, campaignId: Int) =
        withContext(Dispatchers.IO) {
            api.addBeneficiaryToCampaign(
                beneficiaryId,
                campaignId,
                authorization = preferenceUtil.getNGOToken(),
            )
        }

    private suspend fun getBeneficiaryByOrganisation() =
        withContext(Dispatchers.IO) {
            api.getBeneficiariesByOrganisation(
                organisationId = preferenceUtil.getNGOId(),
                authorization = preferenceUtil.getNGOToken(),
            )
        }

    private suspend fun getBeneficiariesByCampaign(campaignId: Int) = withContext(Dispatchers.IO) {
        api.getBeneficiariesByCampaign(
            campaignId = campaignId,
            organisationId = preferenceUtil.getNGOId(),
            authorization = preferenceUtil.getNGOToken(),
        )
    }

    suspend fun getBeneficiariesForUi(campaignId: Int) = supervisorScope {
        val allBeneficiariesDef = async {
            try {
                getBeneficiaryByOrganisation().data!!
            } catch (t: Throwable) {
                Timber.e(t)
                emptyList()
            }
        }
        val campaignBeneficiariesDef = async {
            try {
                getBeneficiariesByCampaign(campaignId = campaignId).data!!
            } catch (t: Throwable) {
                Timber.e(t)
                emptyList()
            }
        }

        val allBeneficiaries = allBeneficiariesDef.await()
        val campaignBeneficiaries = campaignBeneficiariesDef.await()
        allBeneficiaries.map {
            BeneficiaryUi(
                id = it.id!!,
                email = it.email,
                phone = it.phone,
                firstName = it.firstName,
                lastName = it.lastName,
                profilePic = it.profilePic,
                isAdded = campaignBeneficiaries.contains(it),
            )
        }
    }
}

sealed class NetworkResponse<out T>() {

    data class Success<out T>(val body: T, val _message: String = "SUCCESS") : NetworkResponse<T>()
    data class Error<out T>(val _message: String, val e: Throwable) : NetworkResponse<T>()
    data class SimpleError<out T>(val _message: String, val code: Int = 0) : NetworkResponse<T>()

    class NetworkError<out T>() : NetworkResponse<T>()

    class Loading<out T>() : NetworkResponse<T>()
    class Offline<out T>() : NetworkResponse<T>()
}
