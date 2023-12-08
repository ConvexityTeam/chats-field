package chats.cash.chats_field.network.api.interfaces

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.body.impact_report.ImpactReportBody
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.network.response.VendorDetailsResponse
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.group_beneficiary.OnboardGroupBeneficiaryResponse
import chats.cash.chats_field.network.response.impact_report.ImpactReportResponse
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ConvexityDataSourceInterface {

    /**
     * get all the campaigns for the organization that is signed in to the user
     * @return Flow<NetworkResponse<List<ModelCampaign>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getAllCampaigns(
        id: Int,
        token: String,
    ): Flow<NetworkResponse<List<ModelCampaign>>>

    suspend fun uploadImage(
        file: File,
    ): Flow<NetworkResponse<String>>

    /**
     * get all the campaigns survey questions for id of campaign passed
     * @param campaignId the id of the campaign to get questions for
     * @return Flow<NetworkResponse<CampaignSurveyResponseData>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getCampaignSurvey(
        campaignId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<CampaignSurveyResponse.CampaignSurveyResponseData>>

    /**
     * get all the campaigns forms for the organization that is signed in to the user, this data
     * is also stored locally and filtered during beneficiary onboarding to get the survey for the
     * campaign the beneficiary selected
     * @return Flow<NetworkResponse<List<CampaignForm>> which can be collected and observe for
     * loading, success or failure
     */
    suspend fun getAllCampaignForms(
        ngoId: Int,
        ngoToken: String,
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
        ngoId: Int,
        ngoToken: String,
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
        ngoId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>>

    /**
     * onboards a new group beneficiary to the database
     * @param body the body of the group beneficiary to be onboarded,
     * a group beneficiary can have child beneficiaries under them,
     * this checks if its a special case or not
     * and class the appropriate endpoint
     * @return Flow<NetworkResponse<String>> which can be collected and observe for
     * loading, success or failure, the returned string is the new user id
     */
    suspend fun OnboardGroupBeneficiary(
        body: GroupBeneficiaryBody,
        isOnline: Boolean,
        ngoId: Int,
        ngoToken: String,
    ): Flow<NetworkResponse<OnboardGroupBeneficiaryResponse.OnboardGroupBeneficiaryData>>

    /**
     * returns details ofa vendor
     * @param loginBody the body of the password and email
     * @return [kotlinx.coroutines.flow.Flow]<[chats.cash.chats_field.network.NetworkResponse]
     * <[chats.cash.chats_field.network.response.login.LoginResponse]>>]>> which can be collected and observe for
     * loading, success or failure, the returned signed in ngo account
     */
    suspend fun loginNGO(loginBody: LoginBody, onLoggedIn: (LoginResponse) -> Unit):
        Flow<NetworkResponse<LoginResponse>>

    /**
     * resets password of an ngo account by sending email
     * @param email email of the account to send password reset to
     * @return public final data class ForgotPasswordResponse(
     *     val message: String,
     *     val status: String
     * )
     *   [chats.cash.chats_field.network.response.forgot]
     */
    suspend fun sendForgotEmail(email: String): Flow<NetworkResponse<ForgotPasswordResponse>>

    /**
     * returns details ofa vendor
     * @param id the id of the vendor who we are getting his details
     * @return [chats.cash.chats_field.network.response] which can be collected and observe for
     * loading, success or failure, the returned string is the new user id
     */
    suspend fun getVendorDetails(
        id: String,
        token: String,
    ): Flow<NetworkResponse<VendorDetailsResponse>>

    suspend fun answerSurvey(answer: SubmitSurveyAnswerBody, auth: String)

    suspend fun submitImpactReport(body: ImpactReportBody, auth: String): Flow<NetworkResponse<ImpactReportResponse>>
}
