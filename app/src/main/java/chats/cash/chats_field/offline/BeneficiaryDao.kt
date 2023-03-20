package chats.cash.chats_field.offline

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import kotlinx.coroutines.flow.Flow

@Dao
interface BeneficiaryDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertBeneficiary(beneficiary: Beneficiary)

    @Insert(onConflict = REPLACE)
    suspend fun insertCampaigns(campaigns: List<Campaign>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCampaigns(allCampaigns: List<ModelCampaign>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCampaignsForms(allCampaigns: List<CampaignForm>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCashForWork(allCampaigns: List<ModelCampaign>)

    @Delete
    suspend fun deleteBeneficiary(beneficiary: Beneficiary)

    @Query("SELECT * FROM beneficiary")
    fun getAllBeneficiary() : LiveData<List<Beneficiary>>

    @Query("SELECT * FROM campaign")
    fun getCampaigns() : LiveData<List<Campaign>>

    @Query("SELECT * FROM allCampaignForm")
    fun getAllCampaignForms() : Flow<List<CampaignForm>>

    @Query("SELECT * FROM modelCampaign WHERE type is :type")
    fun geAllLiveCampaigns(type: String): LiveData<List<ModelCampaign>>

    @Query("DELETE FROM campaign")
    suspend fun deleteCampaigns()

    @Query("DELETE FROM modelCampaign")
    suspend fun deleteModelCampaigns()

    @Query("DELETE FROM allCampaignForm")
    suspend fun deleteAllCampaignsForms()

    @Query("DELETE FROM beneficiary")
    suspend fun deleteBeneficiaries()
}
