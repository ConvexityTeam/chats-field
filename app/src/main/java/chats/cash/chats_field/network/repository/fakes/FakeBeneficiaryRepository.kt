package chats.cash.chats_field.network.repository.fakes

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.AllCampaignFormResponse
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.interfaces.BeneficiaryInterface
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.campaign.GetAllCampaignsResponse
import chats.cash.chats_field.offline.OfflineRepository
import chats.cash.chats_field.utils.PreferenceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import retrofit2.Response
import timber.log.Timber

class FakeBeneficiaryRepository():BeneficiaryInterface {

    val allCampaigns = listOf(ModelCampaign(1,2,null,null,null,null,null,null,null,null,
    null,null,null,null,null,"1sec"),
        ModelCampaign(2,2,null,null,null,null,null,null,null,null,
            null,null,null,null,null,"1sec")
        )

    val campaignSurvey = CampaignSurveyResponse.CampaignSurveyResponseData(1,2,"",1,2, emptyList(),"2sec")

    val allCampaignSurvey = listOf(CampaignForm(1,allCampaigns,"2sec",1,2, emptyList(),"","2sec"))

    override suspend fun getAllCampaigns(): Flow<NetworkResponse<List<ModelCampaign>>> = flow{

            emit(NetworkResponse.Success(allCampaigns))

    }


    override suspend fun getCampaignSurvey(campaignId: Int): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>> =flow {
       emit(NetworkResponse.Success(campaignSurvey) )
    }

    override suspend fun getAllCampaignForms(): Flow<NetworkResponse<List<CampaignForm>>> = flow {
        emit( NetworkResponse.Success(allCampaignSurvey))
    }

}