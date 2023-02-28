package chats.cash.chats_field.network.api.interfaces

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.AllCampaignFormResponse
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BeneficiaryInterface {

    /**
     * get all the campaigns for the organization that is signed in to the user
     * @return Flow<NetworkResponse<List<ModelCampaign>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getAllCampaigns(

    ): Flow<NetworkResponse<List<ModelCampaign>>>

    /**
     * get all the campaigns survey questions for id of campaign passed
     * @param campaignId the id of the campaign to get questions for
     * @return Flow<NetworkResponse<CampaignSurveyResponseData>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getCampaignSurvey(
        campaignId: Int,
     ):Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>>


    /**
     * get all the campaigns forms for the organization that is signed in to the user, this data
     * is also stored locally and filtered during beneficiary onboarding to get the survey for the
     * campaign the beneficiary selected
     * @return Flow<NetworkResponse<List<CampaignForm>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getAllCampaignForms(
    ): Flow<NetworkResponse<List<CampaignForm>>>
}