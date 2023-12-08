package chats.cash.chats_field.network.datasource

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.ConvexityApiService
import chats.cash.chats_field.network.api.imageServices.ImageServiceProvider
import chats.cash.chats_field.network.api.interfaces.ConvexityDataSourceInterface
import chats.cash.chats_field.network.body.LocationBody
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.body.impact_report.ImpactReportBody
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.network.repository.defaultHandler
import chats.cash.chats_field.network.response.VendorDetailsResponse
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.forgot.ForgotBody
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.group_beneficiary.OnboardGroupBeneficiaryResponse
import chats.cash.chats_field.network.response.impact_report.ImpactReportResponse
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.utils.ImageUploadCallback
import chats.cash.chats_field.utils.ProgressRequestBody
import chats.cash.chats_field.utils.toFile
import com.google.gson.Gson
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.util.*

class RetrofitDataSource(
    private val api: ConvexityApiService,
    private val unknownError: String,
    private val socketError: String,
    private val imageServiceProvider: ImageServiceProvider,
    private val networkError: String,
) : ConvexityDataSourceInterface {

    override suspend fun getAllCampaigns(
        id: Int,
        token: String,
    ): Flow<NetworkResponse<List<ModelCampaign>>> = flow {
        val response = api.getAllCampaigns(
            id,
            null,
            authorization = token,
        )
        Timber.tag("HI").d(response.data.toString())

        if (response.code.toString().startsWith("2")) {
            emit(NetworkResponse.Success(response.data.data))
        } else {
            emit(NetworkResponse.SimpleError(response.message ?: unknownError))
        }
    }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun uploadImage(file: File): Flow<NetworkResponse<String>> = flow {
        val mBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val image = MultipartBody.Part.createFormData(
            "profile_pic",
            file.absolutePath.substringAfterLast("/"),
            mBody,
        )
        val response = api.UploadImage(image, "")

        if (response.code.toString().startsWith("2")) {
            file.delete()
            emit(NetworkResponse.Success(response.link))
        } else {
            emit(NetworkResponse.SimpleError(response.message ?: unknownError))
        }
    }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun getCampaignSurvey(
        campaignId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>> =
        flow {
            val response = api.getCampaignSurvey2(
                campaignId,
                authorization = ngoToken,
            )
            if (response.code.toString().startsWith("2")) {
                emit(NetworkResponse.Success(response.campaignSurveyResponseData))
            } else {
                emit(NetworkResponse.SimpleError(response.message ?: unknownError))
            }
        }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun getAllCampaignForms(
        ngoId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<List<CampaignForm>>> = flow {
        val response = api.getAllCampaignForms2(
            ngoId,
            authorization = ngoToken,
        )
        if (response.code.toString().startsWith("2")) {
            emit(NetworkResponse.Success(response.data.data))
        } else {
            emit(NetworkResponse.SimpleError(response.message ?: unknownError))
        }
    }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun OnboardBeneficiary(
        beneficiary: Beneficiary,
        isOnline: Boolean,
        ngoId: Int,
        ngoToken: String,
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
                gender.trim().lowercase(Locale.ROOT)
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mIris =
                iris?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mDate = date.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val compressed = File(profilePic)
            val mBody = compressed.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val image = MultipartBody.Part.createFormData(
                "profile_pic",
                compressed.absolutePath.substringAfterLast("/"),
                mBody,
            )
            val locationBody =
                LocationBody(
                    coordinates = listOf(longitude, latitude),
                    country = "Nigeria",
                )
            val location = Gson().toJson(locationBody)
            val mLocation = location.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mCampaign =
                campaignId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val mNin = nin.trim()
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
                    val imageBody = ProgressRequestBody(
                        f,
                        "image/jpg",
                        object :
                            ImageUploadCallback {
                            override fun onProgressUpdate(percentage: Int) {
                            }
                        },
                    )
                    val finger = MultipartBody.Part.createFormData(
                        "fingerprints",
                        f.absolutePath.substringAfterLast("/"),
                        imageBody,
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
                    iris = mIris,
                    status = mStatus,
                    nin = mNin.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    profile_pic = image,
                    gender = mGender,
                    date = mDate,
                    campaign = mCampaign,
                    //    pin = mPin,
                    authorization = ngoToken,

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
                    iris = mIris,
                    long = mLongitude,
                    location = mLocation,
                    nfc = mNfc,
                    status = mStatus,
                    gender = mGender,
                    date = mDate,
                    campaign = mCampaign,
                    //   pin = mPin,
                    prints = prints,
                    profile_pic = image,
                    authorization = ngoToken,
                )
            }

            if (response.code.toString().startsWith("2")) {
                compressed.delete()
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
    }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun OnboardVendor(
        beneficiary: Beneficiary,
        isOnline: Boolean,
        ngoId: Int,
        ngoToken: String,
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
                                country,
                            ),
                        ),
                        authorization = ngoToken,
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
        }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun OnboardGroupBeneficiary(
        body: GroupBeneficiaryBody,
        isOnline: Boolean,
        ngoId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<OnboardGroupBeneficiaryResponse.OnboardGroupBeneficiaryData>> =
        flow<NetworkResponse<OnboardGroupBeneficiaryResponse.OnboardGroupBeneficiaryData>> {
            Timber.v(body.toString())
            body.representative.apply {
                var bodyToSend = body

                val compressed = File(profilePic)

                val locationBody =
                    LocationBody(
                        coordinates = listOf(longitude, latitude),
                        country = "Nigeria",
                    )
                bodyToSend =
                    body.copy(
                        representative = body.representative.copy(
                            location = Gson().toJson(
                                locationBody,
                            ),
                        ),
                    )

                val mFingers = ArrayList<File>()
                val prints = ArrayList<MultipartBody.Part>()
                if (!isSpecialCase) {
                    mFingers.add(leftThumb.toFile())
                    mFingers.add(leftIndex.toFile())
                    mFingers.add(leftLittle.toFile())
                    mFingers.add(rightThumb.toFile())
                    mFingers.add(rightIndex.toFile())
                    mFingers.add(rightLittle.toFile())
                    mFingers.forEachIndexed { _, f ->
                        val imageBody = ProgressRequestBody(
                            f,
                            "image/jpg",
                            object :
                                ImageUploadCallback {
                                override fun onProgressUpdate(percentage: Int) {
                                }
                            },
                        )
                        val finger = MultipartBody.Part.createFormData(
                            "fingerprints",
                            f.absolutePath.substringAfterLast("/"),
                            imageBody,
                        )
                        prints.add(finger)
                    }
                }

                // Add member data
                val members = body.member.toMutableList()
                members.mapIndexed { index, groupBeneficiaryMember ->

                    return@mapIndexed imageServiceProvider.uploadImage(
                        groupBeneficiaryMember.profile_pic,
                        "${body.representative.email}${groupBeneficiaryMember.full_name}",
                        true,

                    ) {
                    }.also {
                        if (it.await().isNullOrEmpty().not()) {
                            members[index] = members[index].copy(profile_pic = it.await()!!)
                        } else {
                            emit(
                                NetworkResponse.SimpleError(
                                    "Couldn't upload profile image for child beneficiary ${index + 1}",
                                    410,
                                ),
                            )
                            return@flow
                        }
                    }
                }.awaitAll()

                Timber.v(members.toString())

                val representativeLink = imageServiceProvider.uploadImage(
                    profilePic,
                    bodyToSend.representative.email,
                    true,

                ) {
                }.await()

                if (representativeLink.isNullOrEmpty()) {
                    emit(
                        NetworkResponse.SimpleError(
                            "Couldn't upload profile image for representative beneficiary ",
                            410,
                        ),
                    )
                    return@flow
                }

                bodyToSend = bodyToSend.copy(
                    member = members,
                    representative = bodyToSend.representative
                        .copy(profilePic = representativeLink, id = null),
                )

                val response = api.onboardGroupBeneficiary(
                    bodyToSend,
                    authorization = ngoToken,
                )

                Timber.tag("RESPONSE").v(response.toString())

                if (response.code.toString().startsWith("2")) {
                    emit(NetworkResponse.Success(response.response))
                    if (!isSpecialCase) {
                        try {
                            File(leftLittle).delete()
                            File(leftIndex).delete()
                            File(leftThumb).delete()
                            File(rightLittle).delete()
                            File(rightIndex).delete()
                            File(rightThumb).delete()
                            File(profilePic).delete()
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
        }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun getVendorDetails(
        id: String,
        token: String,
    ): Flow<NetworkResponse<VendorDetailsResponse>> = flow<NetworkResponse<VendorDetailsResponse>> {
        val data = api.getVendorDetails(id, authorization = token)
        emit(NetworkResponse.Success(data))
    }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun sendForgotEmail(email: String): Flow<NetworkResponse<ForgotPasswordResponse>> =
        flow<NetworkResponse<ForgotPasswordResponse>> {
            val forgotBody = ForgotBody(email)
            val data = api.sendForgotMail(forgotBody)
            emit(NetworkResponse.Success(data))
        }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun loginNGO(loginBody: LoginBody, onLoggedIn: (LoginResponse) -> Unit):
        Flow<NetworkResponse<LoginResponse>> = flow<NetworkResponse<LoginResponse>> {
        val data = api.loginNGO(loginBody = loginBody)
        onLoggedIn(data)
        emit(NetworkResponse.Success(data))
        return@flow
    }.defaultHandler(unknownError, socketError, networkError)

    override suspend fun answerSurvey(answer: SubmitSurveyAnswerBody, auth: String) {
        api.answerCampaignSurvey(answer, answer.campaignId, auth)
    }

    override suspend fun submitImpactReport(
        body: ImpactReportBody,
        auth: String,
    ): Flow<NetworkResponse<ImpactReportResponse>> = flow {
        val response = api.submitImpactReport(body, auth)

        if (response.code.toString().startsWith("2")) {
            emit(NetworkResponse.Success(response))
        } else if (response.code == 401) {
            emit(NetworkResponse.SimpleError(response.message, code = response.code))
        } else {
            Timber.v(response.toString())
            emit(NetworkResponse.SimpleError(response.message, code = response.code))
        }
    }.defaultHandler(unknownError, socketError, networkError)
}
