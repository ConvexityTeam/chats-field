package chats.cash.chats_field.network.repository.fakes

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.interfaces.BeneficiaryRepositoryInterface
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.group_beneficiary.OnboardGroupBeneficiaryResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBeneficiaryRepository() : BeneficiaryRepositoryInterface {

    val allCampaigns = listOf(
        ModelCampaign(
            1, 2, null, null, null, null, null, null, null,
            null, null, null, null, null, "1sec",
        ),
        ModelCampaign(
            2, 2, null, null, null, null, null, null, null,
            null, null, null, null, null, "1sec",
        ),
    )

    val campaignSurvey = CampaignSurveyResponse.CampaignSurveyResponseData(
        1,
        2,
        "",
        1,
        2,
        emptyList(),
        "2sec",
    )

    val allCampaignSurvey = listOf(
        CampaignForm(1, allCampaigns, "2sec", 1, 2, emptyList(), "", "2sec"),
    )

    override suspend fun getAllCampaigns(): Flow<NetworkResponse<List<ModelCampaign>>> = flow {
        emit(NetworkResponse.Success(allCampaigns))
    }

    override suspend fun getCampaignSurvey(campaignId: Int): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>> = flow {
        emit(NetworkResponse.Success(campaignSurvey))
    }

    override suspend fun getAllCampaignForms(): Flow<NetworkResponse<List<CampaignForm>>> = flow {
        emit(NetworkResponse.Success(allCampaignSurvey))
    }

    override suspend fun OnboardBeneficiary(
        beneficiary: Beneficiary,
        isOnline: Boolean,
    ): Flow<NetworkResponse<String>> = flow {
    }

    override suspend fun OnboardGroupBeneficiary(
        body: GroupBeneficiaryBody,
        isOnline: Boolean,
    ): Flow<NetworkResponse<OnboardGroupBeneficiaryResponse.OnboardGroupBeneficiaryData>> {
        TODO("Not yet implemented")
    }

    override suspend fun OnboardVendor(
        beneficiary: Beneficiary,
        isOnline: Boolean,
    ): Flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>> = flow {
    }
}
