package chats.cash.chats_field.views.beneficiary_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.response.beneficiary_onboarding.Beneficiary
import chats.cash.chats_field.utils.ChatsFieldConstants.API_SUCCESS
import chats.cash.chats_field.utils.handleThrowable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class BeneficiaryListViewModel(
    private val repository: NetworkRepository,
    campaignId: Int,
) : ViewModel() {

    private val _uiState = MutableLiveData<BeneficiaryListUiState>()
    val uiState: LiveData<BeneficiaryListUiState> get() = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = BeneficiaryListUiState.Error(throwable.handleThrowable())
    }

    init {
        getBeneficiariesByCampaign(campaignId)
    }

    private fun getBeneficiariesByCampaign(campaignId: Int) {
        _uiState.value = BeneficiaryListUiState.Loading
        viewModelScope.launch(exceptionHandler) {
            val beneficiaries = repository.getBeneficiariesForUi(campaignId)
            if (beneficiaries.isNotEmpty()) {
                _uiState.postValue(BeneficiaryListUiState.Success(beneficiaries))
            } else {
                _uiState.postValue(BeneficiaryListUiState.Error("No beneficiaries"))
            }
        }
    }

    fun addBeneficiaryToCampaign(beneficiaryId: Int, campaignId: Int) {
        _uiState.value = BeneficiaryListUiState.AddBeneficiaryLoading
        viewModelScope.launch(exceptionHandler) {
            val response = repository.addBeneficiaryToCampaign(beneficiaryId = beneficiaryId,
                campaignId = campaignId)
            if (response.code in 200..201 && response.status == API_SUCCESS) {
                _uiState.postValue(BeneficiaryListUiState.AddBeneficiarySuccess(response.message))
            } else {
                _uiState.postValue(BeneficiaryListUiState.AddBeneficiaryError(response.message))
            }
        }
    }

    private fun Beneficiary.mapToBeneficiaryUi(): BeneficiaryUi {
        return BeneficiaryUi(
            email = this.email,
            lastName = this.lastName,
            firstName = this.firstName,
            id = this.id,
            phone = this.phone,
            profilePic = this.profilePic,
            isAdded = false
        )
    }

    sealed class BeneficiaryListUiState {
        object Loading : BeneficiaryListUiState()
        data class Success(val beneficiaries: List<BeneficiaryUi>) : BeneficiaryListUiState()
        data class Error(val errorMessage: String?) : BeneficiaryListUiState()
        data class AddBeneficiarySuccess(val message: String) : BeneficiaryListUiState()
        data class AddBeneficiaryError(val errorMessage: String?) : BeneficiaryListUiState()
        object AddBeneficiaryLoading : BeneficiaryListUiState()
    }
}
