package com.codose.chats.offline

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.response.organization.campaign.Campaign

@Dao
interface BeneficiaryDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertBeneficiary(beneficiary: Beneficiary)

    @Insert(onConflict = REPLACE)
    suspend fun insertCampaigns(campaigns: List<Campaign>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCampaigns(allCampaigns: List<ModelCampaign>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllCashForWork(allCampaigns: List<ModelCampaign>)

    @Delete
    suspend fun deleteBeneficiary(beneficiary: Beneficiary)

    @Query("SELECT * FROM beneficiary")
    fun getAllBeneficiary() : LiveData<List<Beneficiary>>

    @Query("SELECT * FROM campaign")
    fun getCampaigns() : LiveData<List<Campaign>>

    @Query("SELECT * FROM modelCampaign WHERE type is :type")
    fun geAllLiveCampaigns(type: String): LiveData<List<ModelCampaign>>

    @Query("DELETE FROM campaign")
    suspend fun deleteCampaigns()

    @Query("DELETE FROM modelCampaign")
    suspend fun deleteModelCampaigns()

    @Query("DELETE FROM beneficiary")
    suspend fun deleteBeneficiaries()
}
