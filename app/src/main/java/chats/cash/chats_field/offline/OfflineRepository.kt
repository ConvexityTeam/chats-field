package chats.cash.chats_field.offline

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.network.response.beneficiary_onboarding.OrganizationBeneficiary
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineRepository(private val beneficiaryDao: BeneficiaryDao) {

    @WorkerThread
    suspend fun insert(beneficiary: Beneficiary) {
        beneficiaryDao.insertBeneficiary(beneficiary)
    }

    @WorkerThread
    suspend fun insert(beneficiary: GroupBeneficiaryBody) {
        beneficiaryDao.insertGroupBeneficiary(beneficiary)
    }

    @WorkerThread
    suspend fun insertCampaign(campaign: List<Campaign>) {
        beneficiaryDao.insertCampaigns(campaign)
    }

    @WorkerThread
    suspend fun insertAllCampaign(campaigns: List<ModelCampaign>) {
        beneficiaryDao.deleteModelCampaigns()
        beneficiaryDao.insertAllCampaigns(campaigns)
    }

    @WorkerThread
    suspend fun insertAllCampaignForms(campaigns: List<CampaignForm>) {
        beneficiaryDao.insertAllCampaignsForms(campaigns)
    }

    @WorkerThread
    suspend fun insertAllCashForWork(campaigns: List<ModelCampaign>) {
        beneficiaryDao.insertAllCashForWork(campaigns)
    }

    @WorkerThread
    suspend fun delete(beneficiary: Beneficiary) {
        beneficiaryDao.deleteBeneficiary(beneficiary)
    }

    @WorkerThread
    suspend fun delete(beneficiary: GroupBeneficiaryBody) {
        beneficiaryDao.deleteBeneficiary(beneficiary)
    }

    @WorkerThread
    suspend fun deleteAllCampaignForms() {
        beneficiaryDao.deleteAllCampaignsForms()
    }

    fun getAllBeneficiary(): LiveData<List<Beneficiary>> {
        return beneficiaryDao.getAllBeneficiary()
    }

    fun getAllGroupBeneficiary(): LiveData<List<GroupBeneficiaryBody>> {
        return beneficiaryDao.getAllGroupBeneficiary()
    }

    fun getAllCampaigns(type: String): LiveData<List<ModelCampaign>> {
        return beneficiaryDao.geAllLiveCampaigns(type)
    }

    fun getAllCampaigns(): LiveData<List<ModelCampaign>> {
        return beneficiaryDao.geAllLiveCampaigns()
    }

    fun getCampaignsForm(): Flow<List<CampaignForm>> {
        return beneficiaryDao.getAllCampaignForms()
    }

    suspend fun getCampaignsAnswer(email: String): SubmitSurveyAnswerBody? {
        return beneficiaryDao.getSurveyAnswer(email)
    }

    suspend fun insertCampaignsAnswer(answer: SubmitSurveyAnswerBody) {
        return beneficiaryDao.insertSurveyAnswer(answer)
    }

    suspend fun deleteCampaignsAnswer(answer: SubmitSurveyAnswerBody) {
        return beneficiaryDao.deleteSurveyAnswer(answer)
    }

    suspend fun deleteAllTables() = CoroutineScope(Dispatchers.IO).launch {
        val del1Def = async { beneficiaryDao.deleteCampaigns() }
        val del2Def = async { beneficiaryDao.deleteModelCampaigns() }
        val del3Def = async { beneficiaryDao.deleteBeneficiaries() }
        listOf(del1Def, del2Def, del3Def).awaitAll()
    }

    suspend fun insertIrisSignature(iris: String) {
        beneficiaryDao.insertOrganizationBeneficiary(
            OrganizationBeneficiary(
                dob = null,
                email = null,
                firstName = null,
                gender = null,
                lastName = null,
                maritalStatus = null,
                phone = null,
                profilePic = null,
                iris = iris,
            ),
        )
    }

    suspend fun getAllOrgnaizationBeneficiaries(): List<OrganizationBeneficiary> {
        return withContext(Dispatchers.IO) {
            return@withContext beneficiaryDao.getAllOrganizationBeneficiary()
        }
    }
}
