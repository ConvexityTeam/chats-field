package chats.cash.chats_field.network.repository

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.interfaces.BeneficiaryInterface
import chats.cash.chats_field.network.api.interfaces.BeneficiaryRepositoryInterface
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineRepository
import chats.cash.chats_field.utils.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber

class BeneficiaryRepository(
    private val dataSource: RetrofitDataSource,
    private val offlineRepository: OfflineRepository,
    private val preferenceUtil: PreferenceUtil,
) : BeneficiaryRepositoryInterface {

    override suspend fun getAllCampaigns(): Flow<NetworkResponse<List<ModelCampaign>>> {
        return dataSource.getAllCampaigns(
            preferenceUtil.getNGOId(),
            preferenceUtil.getNGOToken()
        ).onEach {
            if (it is NetworkResponse.Success) {
                val body = it.body
                if (body.isNotEmpty()) {
                    offlineRepository.insertAllCampaign(body)
                }
            }
        }
    }


    override suspend fun getCampaignSurvey(campaignId: Int): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>> {

        return dataSource.getCampaignSurvey((campaignId), preferenceUtil.getNGOToken())
    }

    override suspend fun getAllCampaignForms(): Flow<NetworkResponse<List<CampaignForm>>> {

        return dataSource.getAllCampaignForms(
            preferenceUtil.getNGOId(),
            preferenceUtil.getNGOToken()
        ).onEach {
            if (it is NetworkResponse.Success) {

                val data = it.body
                if (data.isNotEmpty()) {
                    offlineRepository.deleteAllCampaignForms()
                    offlineRepository.insertAllCampaignForms(data)
                }
            }
        }

    }

    override suspend fun OnboardBeneficiary(
        beneficiary: Beneficiary,
        isOnline: Boolean,
    ): Flow<NetworkResponse<String>> {
        Timber.v("here")
        if (!isOnline) {
            offlineRepository.insert(beneficiary)
        }
        return if (isOnline) dataSource.OnboardBeneficiary(
            beneficiary, true, preferenceUtil.getNGOId(),
            preferenceUtil.getNGOToken()
        ).onEach { result ->
            if (result is NetworkResponse.Success) {
                val res = result.body
                Timber.v("id is $res")
                val campaignFormAnswer =
                    offlineRepository.getCampaignsAnswer(beneficiary.email)
                        ?.copy(beneficiaryId = res.toInt())
                campaignFormAnswer?.let {
                    dataSource.answerSurvey(it, preferenceUtil.getNGOToken())
                    offlineRepository.deleteCampaignsAnswer(it)
                }
            }
            result
        } else flow {
            emit(NetworkResponse.Offline())
        }
    }

    override suspend fun OnboardVendor(
        beneficiary: Beneficiary,
        isOnline: Boolean,
    ): Flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>> {
        if (!isOnline) {
            offlineRepository.insert(beneficiary)
        }
        return if (isOnline) dataSource.OnboardVendor(
            beneficiary, true,
            preferenceUtil.getNGOId(), preferenceUtil.getNGOToken()
        ) else flow {
            emit(NetworkResponse.Offline())
        }
    }


}

const val OFFLINE_RESPONSE = "OFFLINE"