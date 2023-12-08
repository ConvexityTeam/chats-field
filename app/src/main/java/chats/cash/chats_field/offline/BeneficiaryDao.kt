package chats.cash.chats_field.offline

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.network.response.beneficiary_onboarding.OrganizationBeneficiary
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import kotlinx.coroutines.flow.Flow

@Dao
interface BeneficiaryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBeneficiary(beneficiary: Beneficiary)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroupBeneficiary(beneficiary: GroupBeneficiaryBody)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizationBeneficiary(beneficiary: OrganizationBeneficiary)

    @Insert(onConflict = REPLACE)
    suspend fun insertCampaigns(campaigns: List<Campaign>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCampaigns(allCampaigns: List<ModelCampaign>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCampaignsForms(allCampaigns: List<CampaignForm>)

    @Insert(onConflict = REPLACE)
    suspend fun insertSurveyAnswer(answer: SubmitSurveyAnswerBody)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCashForWork(allCampaigns: List<ModelCampaign>)

    @Delete
    suspend fun deleteBeneficiary(beneficiary: Beneficiary)

    @Delete
    suspend fun deleteBeneficiary(beneficiary: GroupBeneficiaryBody)

    @Delete
    suspend fun deleteSurveyAnswer(answer: SubmitSurveyAnswerBody)

    @Query("SELECT * FROM beneficiary")
    fun getAllBeneficiary(): LiveData<List<Beneficiary>>

    @Query("SELECT * FROM `group beneficiary`")
    fun getAllGroupBeneficiary(): LiveData<List<GroupBeneficiaryBody>>

    @Query("SELECT * FROM organization_beneficiary")
    fun getAllOrganizationBeneficiary(): List<OrganizationBeneficiary>

    @Query("SELECT * FROM campaign")
    fun getCampaigns(): LiveData<List<Campaign>>

    @Query("SELECT * FROM allCampaignForm")
    fun getAllCampaignForms(): Flow<List<CampaignForm>>

    @Query("SELECT * FROM survey_answers where email = :email")
    suspend fun getSurveyAnswer(email: String): SubmitSurveyAnswerBody?

    @Query("SELECT * FROM modelCampaign WHERE type is :type")
    fun geAllLiveCampaigns(type: String): LiveData<List<ModelCampaign>>

    @Query("SELECT * FROM modelCampaign")
    fun geAllLiveCampaigns(): LiveData<List<ModelCampaign>>

    @Query("DELETE FROM campaign")
    suspend fun deleteCampaigns()

    @Query("DELETE FROM modelCampaign")
    suspend fun deleteModelCampaigns()

    @Query("DELETE FROM allCampaignForm")
    suspend fun deleteAllCampaignsForms()

    @Query("DELETE FROM beneficiary")
    suspend fun deleteBeneficiaries()
}
