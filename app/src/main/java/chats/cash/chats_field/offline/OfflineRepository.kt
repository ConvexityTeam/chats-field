package chats.cash.chats_field.offline

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class OfflineRepository(private val beneficiaryDao: BeneficiaryDao?) {

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
    suspend fun insertAllCashForWork(campaigns: List<ModelCampaign>) {
        beneficiaryDao?.insertAllCashForWork(campaigns)
    }


    @WorkerThread
    suspend fun delete(beneficiary : Beneficiary){
        beneficiaryDao?.deleteBeneficiary(beneficiary)
    }

    fun getAllBeneficiary() : LiveData<List<Beneficiary>>{
        return beneficiaryDao!!.getAllBeneficiary()
    }

    fun getAllCampaigns(type: String) : LiveData<List<ModelCampaign>>{
        return beneficiaryDao!!.geAllLiveCampaigns(type)
    }

    suspend fun deleteAllTables() {
        coroutineScope {
            val del1Def = async { beneficiaryDao?.deleteCampaigns() }
            val del2Def = async { beneficiaryDao?.deleteModelCampaigns() }
            val del3Def = async { beneficiaryDao?.deleteBeneficiaries() }
            listOf(del1Def, del2Def, del3Def).awaitAll()
        }
    }
}