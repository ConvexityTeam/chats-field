package com.codose.chats.offline

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.response.organization.campaign.Campaign

@Dao
interface BeneficiaryDao {
    @Insert
    fun insertBeneficiary(beneficiary: Beneficiary)

    @Insert
    fun insertCampaigns(campaigns: List<Campaign>)

    @Insert
    fun insertAllCampaigns(allCampaigns: List<ModelCampaign>)

    @Insert
    fun insertAllCashForWork(allCampaigns: List<ModelCampaign>)

    @Delete
    fun deleteBeneficiary(beneficiary: Beneficiary)

    @Query("SELECT * FROM beneficiary")
    fun getAllBeneficiary() : LiveData<List<Beneficiary>>


    @Query("SELECT * FROM campaign")
    fun getCampaigns() : LiveData<List<Campaign>>

    @Query("SELECT * FROM modelCampaign")
    fun geAllLiveCampaigns(): LiveData<List<ModelCampaign>>


}