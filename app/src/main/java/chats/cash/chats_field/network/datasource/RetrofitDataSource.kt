package chats.cash.chats_field.network.datasource

import chats.cash.chats_field.R
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.AllCampaignFormResponse
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.ConvexityApiService
import chats.cash.chats_field.network.api.interfaces.BeneficiaryInterface
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.utils.PreferenceUtil
import chats.cash.chats_field.views.core.strings.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class RetrofitDataSource(
    private val api: ConvexityApiService,
    private val preferenceUtil: PreferenceUtil,
    private val uiText: UiText
):BeneficiaryInterface {

    private val unknownError =  uiText.getStringResource(R.string.an_unknown_error_occured)


    override suspend fun getAllCampaigns(
    ): Flow<NetworkResponse<List<ModelCampaign>>> =flow{

        val response = api.getAllCampaigns(preferenceUtil.getNGOId(),
            null,
            authorization = preferenceUtil.getNGOToken())

        if(response.code.toString().startsWith("2")){
            emit(NetworkResponse.Success(response.data))
        }
        else{
            emit(NetworkResponse.SimpleError(response.message?:unknownError))
        }

    }.catch {
        emit(NetworkResponse.Error(it.message?:unknownError,it))
    }.onStart {
        emit(NetworkResponse.Loading())
    }


    override suspend fun getCampaignSurvey(campaignId: Int): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>> = flow {

        val response = api.getCampaignSurvey2(campaignId,
                authorization = preferenceUtil.getNGOToken())

        if(response.code.toString().startsWith("2")){
            emit(NetworkResponse.Success(response.campaignSurveyResponseData))
        }
        else{
            emit(NetworkResponse.SimpleError(response.message?:unknownError))
        }

    }.catch {
        emit(NetworkResponse.Error(it.message?:unknownError,it))
    }.onStart {
        emit(NetworkResponse.Loading())
    }


    override suspend fun getAllCampaignForms(): Flow<NetworkResponse<List<CampaignForm>>> = flow {
        val response =  api.getAllCampaignForms2(preferenceUtil.getNGOId(),
            authorization = preferenceUtil.getNGOToken())

        if(response.code.toString().startsWith("2")){
            emit(NetworkResponse.Success(response.data))
        }
        else{
            emit(NetworkResponse.SimpleError(response.message?:unknownError))
        }


    }.catch {
        emit(NetworkResponse.Error(it.message?:unknownError,it))
    }.onStart {
        emit(NetworkResponse.Loading())
    }


}