package chats.cash.chats_field.views.beneficiary_onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.api.interfaces.BeneficiaryRepositoryInterface
import chats.cash.chats_field.utils.ChatsFieldConstants.API_SUCCESS
import chats.cash.chats_field.utils.handleThrowable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class ExistingBeneficiaryViewModel(
    private val repository: NetworkRepository,
    private val beneficiaryRepository: BeneficiaryRepositoryInterface,
) : ViewModel() {

    private val _uiState = MutableLiveData<ExistingBeneficiaryUiState>()
    val uiState: LiveData<ExistingBeneficiaryUiState> = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = ExistingBeneficiaryUiState.Error(throwable.handleThrowable())
    }

    init {
        viewModelScope.launch(exceptionHandler) {
            beneficiaryRepository.getAllCampaigns()
        }
    }

    fun addBeneficiaryToCampaign(beneficiaryId: Int, campaignId: Int) {
        _uiState.value = ExistingBeneficiaryUiState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = repository.addBeneficiaryToCampaign(
                beneficiaryId = beneficiaryId,
                campaignId = campaignId,
            )
            if (response.status == API_SUCCESS && response.code in 200..201) {
                _uiState.postValue(ExistingBeneficiaryUiState.Success(message = response.message))
            } else {
                _uiState.postValue(
                    ExistingBeneficiaryUiState.Error(errorMessage = response.message),
                )
            }
        }
    }

    sealed class ExistingBeneficiaryUiState {
        object Loading : ExistingBeneficiaryUiState()
        data class Error(val errorMessage: String?) : ExistingBeneficiaryUiState()
        data class Success(val message: String) : ExistingBeneficiaryUiState()
    }
}
