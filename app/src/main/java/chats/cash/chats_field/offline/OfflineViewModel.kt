package chats.cash.chats_field.offline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineViewModel(application : Application, private val offlineRepository: OfflineRepository,) : AndroidViewModel(application) {

    val getBeneficiaries = offlineRepository.getAllBeneficiary()
    fun insert(beneficiary: Beneficiary){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                offlineRepository.insert(beneficiary)
            }
        }
    }

    fun delete(beneficiary: Beneficiary){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                offlineRepository.delete(beneficiary)
            }
        }
    }

    fun getAllCampaignForms(): Flow<List<CampaignForm>> {
       return offlineRepository.getCampaignsForm()
    }

    var userCampaignForm = MutableStateFlow<CampaignForm?>(null)
    fun setCampaignForm(it: CampaignForm) {
       userCampaignForm.value = it
    }

    fun insertSurveryAnswer(answer: SubmitSurveyAnswerBody) =viewModelScope.launch{
        offlineRepository.insertCampaignsAnswer(answer)
    }

    fun getCampaignSurveyAnswer(email: String) =viewModelScope.async {
        offlineRepository.getCampaignsAnswer(email)
    }


}