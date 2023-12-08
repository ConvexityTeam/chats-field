package chats.cash.chats_field.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineViewModel(private val offlineRepository: OfflineRepository) : ViewModel() {

    val getBeneficiaries = offlineRepository.getAllBeneficiary()
    val getGroupBeneficiaries = offlineRepository.getAllGroupBeneficiary()
    fun insert(beneficiary: Beneficiary) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                offlineRepository.insert(beneficiary)
            }
        }
    }

    fun delete(beneficiary: Beneficiary) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                offlineRepository.delete(beneficiary)
            }
        }
    }

    fun delete(beneficiary: GroupBeneficiaryBody) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                offlineRepository.delete(beneficiary)
                offlineRepository.delete(beneficiary.representative)
            }
        }
    }

    val allCampaigns = offlineRepository.getAllCampaigns()
    fun getAllCampaignForms(): Flow<List<CampaignForm>> {
        return offlineRepository.getCampaignsForm()
    }

    var userCampaignForm = MutableStateFlow<CampaignForm?>(null)
    fun setCampaignForm(it: CampaignForm) {
        userCampaignForm.value = it
    }

    fun insertSurveryAnswer(answer: SubmitSurveyAnswerBody) = viewModelScope.launch {
        offlineRepository.insertCampaignsAnswer(answer)
    }

    fun getCampaignSurveyAnswer(email: String) = viewModelScope.async {
        offlineRepository.getCampaignsAnswer(email)
    }

    fun insertIrisSignature(iris: String) = viewModelScope.launch {
        offlineRepository.insertIrisSignature(iris)
    }
}

data class SelectCampaignState(
    val loading: Boolean = false,
    val allCampaigns: List<ModelCampaign>,
    val cahsCampaign: List<ModelCampaign>,
    val itemCampaign: List<ModelCampaign>,
)
