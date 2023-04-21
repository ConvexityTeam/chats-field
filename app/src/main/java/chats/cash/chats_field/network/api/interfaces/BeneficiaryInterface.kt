package chats.cash.chats_field.network.api.interfaces

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.AllCampaignFormResponse
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.response.RegisterResponse
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
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
        id: Int, token: String,
    ): Flow<NetworkResponse<List<ModelCampaign>>>

    /**
     * get all the campaigns survey questions for id of campaign passed
     * @param campaignId the id of the campaign to get questions for
     * @return Flow<NetworkResponse<CampaignSurveyResponseData>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getCampaignSurvey(
        campaignId: Int, ngoToken: String,
    ): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>>


    /**
     * get all the campaigns forms for the organization that is signed in to the user, this data
     * is also stored locally and filtered during beneficiary onboarding to get the survey for the
     * campaign the beneficiary selected
     * @return Flow<NetworkResponse<List<CampaignForm>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getAllCampaignForms(
        ngoId: Int, ngoToken: String,
    ): Flow<NetworkResponse<List<CampaignForm>>>

    /**
     * onboards a new beneficiary to the database
     * @param beneficiary the beneficiary to be onboarded, this checks if its a special case or not
     * and class the appropriate endpoint
     * @return Flow<NetworkResponse<String>> which can be collected and observe for
     * loading, success or failure, the returned string is the new user id
     */
    suspend fun OnboardBeneficiary(
        beneficiary: Beneficiary,
        isOnline: Boolean,
        ngoId: Int, ngoToken: String,
    ): Flow<NetworkResponse<String>>

    /**
     * onboards a new beneficiary to the database
     * @param beneficiary the beneficiary to be onboarded, this checks if its a special case or not
     * and class the appropriate endpoint
     * @return Flow<NetworkResponse<String>> which can be collected and observe for
     * loading, success or failure, the returned string is the new user id
     */
    suspend fun OnboardVendor(
        beneficiary: Beneficiary,
        isOnline: Boolean,
        ngoId: Int, ngoToken: String,
    ): Flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>>


}