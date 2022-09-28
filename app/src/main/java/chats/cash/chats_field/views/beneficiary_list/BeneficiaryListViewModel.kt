package chats.cash.chats_field.views.beneficiary_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.response.beneficiary_onboarding.Beneficiary
import chats.cash.chats_field.utils.BluetoothConstants.API_SUCCESS
import chats.cash.chats_field.utils.handleThrowable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class BeneficiaryListViewModel(private val repository: NetworkRepository) : ViewModel() {

    private val _uiState = MutableLiveData<BeneficiaryListUiState>()
    val uiState: LiveData<BeneficiaryListUiState> get() = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = BeneficiaryListUiState.Error(throwable.handleThrowable())
    }

    init {
        _uiState.value = BeneficiaryListUiState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = repository.getBeneficiaryByOrganisation()
            if (response.code in 200..201 && response.status == API_SUCCESS) {
                _uiState.postValue(response.data?.let { BeneficiaryListUiState.Success(it) })
            } else {
                _uiState.postValue(BeneficiaryListUiState.Error(response.message))
            }
        }
    }

    sealed class BeneficiaryListUiState {
        object Loading : BeneficiaryListUiState()
        data class Success(val beneficiaries: List<Beneficiary>) : BeneficiaryListUiState()
        data class Error(val errorMessage: String?) : BeneficiaryListUiState()
    }
}
