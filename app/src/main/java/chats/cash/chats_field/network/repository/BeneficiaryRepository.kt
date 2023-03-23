package chats.cash.chats_field.network.repository

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.interfaces.BeneficiaryInterface
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class BeneficiaryRepository(
    private val dataSource: RetrofitDataSource,
    private val offlineRepository: OfflineRepository,
):BeneficiaryInterface {

    override suspend fun getAllCampaigns(): Flow<NetworkResponse<List<ModelCampaign>>>{
        return dataSource.getAllCampaigns().onEach {
            if(it is NetworkResponse.Success){
                val body = it.body
                if(body.isNotEmpty()){
                    offlineRepository.insertAllCampaign(body)
                }
            }
        }
    }


    override suspend fun getCampaignSurvey(campaignId: Int): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>> {

        return dataSource.getCampaignSurvey((campaignId))
    }

    override suspend fun getAllCampaignForms(): Flow<NetworkResponse<List<CampaignForm>>> {

        return dataSource.getAllCampaignForms().onEach {
            if(it is NetworkResponse.Success){

                val data = it.body
                if(data.isNotEmpty()){
                    offlineRepository.deleteAllCampaignForms()
                    offlineRepository.insertAllCampaignForms(data)
                }
            }
        }

    }

    override suspend fun OnboardBeneficiary(beneficiary: Beneficiary,
                                            isOnline:Boolean): Flow<NetworkResponse<String>> {
        if(!isOnline){
            offlineRepository.insert(beneficiary)
        }
        return if(isOnline) dataSource.OnboardBeneficiary(beneficiary, true) else flow{
            emit(NetworkResponse.Offline())
        }
    }

    override suspend fun OnboardVendor(
        beneficiary: Beneficiary,
        isOnline: Boolean,
    ): Flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>> {
        if(!isOnline){
            offlineRepository.insert(beneficiary)
        }
        return if(isOnline) dataSource.OnboardVendor(beneficiary, true) else flow{
            emit(NetworkResponse.Offline())
        }
    }


}

const val OFFLINE_RESPONSE = "OFFLINE"