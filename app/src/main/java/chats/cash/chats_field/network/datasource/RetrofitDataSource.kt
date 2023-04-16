package chats.cash.chats_field.network.datasource

import chats.cash.chats_field.R
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.ConvexityApiService
import chats.cash.chats_field.network.api.interfaces.BeneficiaryInterface
import chats.cash.chats_field.network.body.LocationBody
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.network.response.RegisterResponse
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.utils.ApiResponse
import chats.cash.chats_field.utils.ImageUploadCallback
import chats.cash.chats_field.utils.PreferenceUtil
import chats.cash.chats_field.utils.ProgressRequestBody
import chats.cash.chats_field.utils.Utils
import chats.cash.chats_field.utils.toFile
import chats.cash.chats_field.views.core.strings.UiText
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.util.*

class RetrofitDataSource(
    private val api: ConvexityApiService,
    private val unknownError: String,
) : BeneficiaryInterface {


    override suspend fun getAllCampaigns(
        id: Int,
        token: String,
    ): Flow<NetworkResponse<List<ModelCampaign>>> = flow {

        val response = api.getAllCampaigns(
            id,
            null,
            authorization = token
        )

        if (response.code.toString().startsWith("2")) {
            emit(NetworkResponse.Success(response.data))
        } else {
            emit(NetworkResponse.SimpleError(response.message ?: unknownError))
        }

    }.catch {
        emit(NetworkResponse.Error(it.message ?: unknownError, it))
    }.onStart {
        emit(NetworkResponse.Loading())
    }


    override suspend fun getCampaignSurvey(
        campaignId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>> =
        flow {

            val response = api.getCampaignSurvey2(
                campaignId,
                authorization = ngoToken
            )

            if (response.code.toString().startsWith("2")) {
                emit(NetworkResponse.Success(response.campaignSurveyResponseData))
            } else {
                emit(NetworkResponse.SimpleError(response.message ?: unknownError))
            }

        }.catch {
            emit(NetworkResponse.Error(it.message ?: unknownError, it))
        }.onStart {
            emit(NetworkResponse.Loading())
        }


    override suspend fun getAllCampaignForms(
        ngoId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<List<CampaignForm>>> = flow {
        val response = api.getAllCampaignForms2(
            ngoId,
            authorization = ngoToken
        )

        if (response.code.toString().startsWith("2")) {
            emit(NetworkResponse.Success(response.data))
        } else {
            emit(NetworkResponse.SimpleError(response.message ?: unknownError))
        }


    }.catch {
        emit(NetworkResponse.Error(it.message ?: unknownError, it))
    }.onStart {
        emit(NetworkResponse.Loading())
    }

    override suspend fun OnboardBeneficiary(
        beneficiary: Beneficiary,
        isOnline: Boolean,
        ngoId: Int, ngoToken: String,
    ): Flow<NetworkResponse<String>> = flow<NetworkResponse<String>> {
        Timber.v(beneficiary.toString())
        beneficiary.apply {
            val mFirstName =
                firstName.trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mLastName = lastName.trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mEmail = email.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mLatitude =
                latitude.toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mLongitude =
                longitude.toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mPhone = phone.trim().also {
                Timber.v(it)
            }.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mPassword = password.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mNfc = nfc.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mStatus = 5.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mGender =
                gender.lowercase(Locale.ROOT)
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mDate = date.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val compressed = File(profilePic)
            val mBody = compressed.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val image = MultipartBody.Part.createFormData(
                "profile_pic",
                compressed.absolutePath.substringAfterLast("/"),
                mBody
            )
            val locationBody =
                LocationBody(
                    coordinates = listOf(longitude, latitude),
                    country = "Nigeria"
                )
            val location = Gson().toJson(locationBody)
            val mLocation = location.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mCampaign =
                campaignId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mNin = nin
            val mPin = pin.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val mFingers = ArrayList<File>()
            val prints = ArrayList<MultipartBody.Part>()
            if (!isSpecialCase) {
                mFingers.add(beneficiary.leftThumb.toFile())
                mFingers.add(beneficiary.leftIndex.toFile())
                mFingers.add(beneficiary.leftLittle.toFile())
                mFingers.add(beneficiary.rightThumb.toFile())
                mFingers.add(beneficiary.rightIndex.toFile())
                mFingers.add(beneficiary.rightLittle.toFile())
                mFingers.forEachIndexed { _, f ->
                    val imageBody = ProgressRequestBody(f, "image/jpg", object :
                        ImageUploadCallback {
                        override fun onProgressUpdate(percentage: Int) {

                        }

                    })
                    val finger = MultipartBody.Part.createFormData(
                        "fingerprints",
                        f.absolutePath.substringAfterLast("/"),
                        imageBody
                    )
                    prints.add(finger)
                }
            }


            val response = if (isSpecialCase) {
                api.onboardSpecialBeneficiary(
                    organisationId = id.toString(),
                    firstName = mFirstName,
                    lastName = mLastName,
                    email = mEmail,
                    phone = mPhone,
                    password = mPassword,
                    lat = mLatitude,
                    long = mLongitude,
                    location = mLocation,
                    nfc = mNfc,
                    status = mStatus,
                    nin = mNin.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    profile_pic = image,
                    gender = mGender,
                    date = mDate,
                    campaign = mCampaign,
                    pin = mPin,
                    authorization = ngoToken

                )
            } else {
                api.onboardBeneficiary(
                    organisationId = id.toString(),
                    firstName = mFirstName,
                    lastName = mLastName,
                    email = mEmail,
                    phone = mPhone,
                    password = mPassword,
                    lat = mLatitude,
                    long = mLongitude,
                    location = mLocation,
                    nfc = mNfc,
                    status = mStatus,
                    profile_pic = image,
                    gender = mGender,
                    date = mDate,
                    campaign = mCampaign,
                    pin = mPin,
                    prints = prints,
                    authorization = ngoToken

                )
            }

            if (response.code.toString().startsWith("2")) {
                emit(NetworkResponse.Success(response.data))
                if (!beneficiary.isSpecialCase) {
                    try {
                        File(beneficiary.leftLittle).delete()
                        File(beneficiary.leftIndex).delete()
                        File(beneficiary.leftThumb).delete()
                        File(beneficiary.rightLittle).delete()
                        File(beneficiary.rightIndex).delete()
                        File(beneficiary.rightThumb).delete()
                        File(beneficiary.profilePic).delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else if (response.code == 401) {
                emit(NetworkResponse.SimpleError(response.message, code = response.code))
            } else {
                Timber.v(response.toString())
                emit(NetworkResponse.SimpleError(response.message, code = response.code))
            }
        }
    }.catch {
        Timber.v(it.toString())
        if(it is HttpException) {
            val message = Utils.getErrorMessage(it)
            emit(NetworkResponse.Error(message , it))
        }
        else{
            emit(NetworkResponse.Error(it.message.toString() , it))
        }
    }.onStart {
        emit(NetworkResponse.Loading())
    }

    override suspend fun OnboardVendor(
        beneficiary: Beneficiary,
        isOnline: Boolean,
        ngoId: Int, ngoToken: String,
    ): Flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>> =
        flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>> {
            if (isOnline) {
                beneficiary.apply {
                    val response = api.vendorOnboarding2(
                        organisationId = ngoId,
                        email = email,
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        address = address.trim(),
                        country = country.trim(),
                        phone = phone.trim(),
                        storeName = storeName,
                        state = state.trim(),
                        location = Gson().toJson(
                            LocationBody(
                                listOf(longitude, latitude),
                                country
                            )
                        ),
                        authorization = ngoToken
                    )

                    if (response.code.toString().startsWith("2")) {
                        emit(NetworkResponse.Success(response.response))
                    } else if (response.code == 401) {
                        emit(NetworkResponse.SimpleError(response.message, code = response.code))
                    } else {
                        emit(NetworkResponse.SimpleError(response.message, code = response.code))
                    }
                }
            } else {
                emit(NetworkResponse.SimpleError("No internet connection"))
            }
        }.catch {
            emit(NetworkResponse.Error(it.message ?: unknownError, it))
        }.onStart {
            emit(NetworkResponse.Loading())
        }

    suspend fun answerSurvey(answer: SubmitSurveyAnswerBody, auth:String)  {
       api.answerCampaignSurvey(answer,answer.campaignId,auth)
    }


}