package com.codose.chats.offline

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.response.organization.campaign.Campaign
import com.codose.chats.offline.Beneficiary
import com.codose.chats.offline.BeneficiaryDao

class OfflineRepository(val beneficiaryDao: BeneficiaryDao?) {

    @WorkerThread
    suspend fun insert(beneficiary : Beneficiary){
        beneficiaryDao?.insertBeneficiary(beneficiary)
    }

    @WorkerThread
    suspend fun insertCampaign(campaign : List<Campaign>){
        beneficiaryDao?.insertCampaigns(campaign)
    }

    @WorkerThread
    suspend fun insertAllCampaign(campaigns: List<ModelCampaign>){
        beneficiaryDao?.insertAllCampaigns(campaigns)
    }

    @WorkerThread
    suspend fun insertAllCashForWork(campaigns: List<ModelCampaign>){
        beneficiaryDao?.insertAllCashForWork(campaigns)
    }


    @WorkerThread
    suspend fun delete(beneficiary : Beneficiary){
        beneficiaryDao?.deleteBeneficiary(beneficiary)
    }

    fun getAllBeneficiary() : LiveData<List<Beneficiary>>{
        return beneficiaryDao!!.getAllBeneficiary()
    }

    fun getAllCampaigns() : LiveData<List<ModelCampaign>>{
        return beneficiaryDao!!.geAllLiveCampaigns()
    }

}