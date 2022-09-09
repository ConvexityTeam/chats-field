package com.codose.chats.views.beneficiary_onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.network.NetworkRepository
import com.codose.chats.utils.BluetoothConstants.API_SUCCESS
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class ExistingBeneficiaryViewModel(
    private val repository: NetworkRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ExistingBeneficiaryUiState>()
    val uiState: LiveData<ExistingBeneficiaryUiState> = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = ExistingBeneficiaryUiState.Error(throwable.localizedMessage)
        Timber.e(throwable)
    }

    init {
        viewModelScope.launch(exceptionHandler) {
            repository.getAllCampaigns()
        }
    }

    fun addBeneficiaryToCampaign(beneficiaryId: Int, campaignId: Int) {
        _uiState.value = ExistingBeneficiaryUiState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = repository.addBeneficiaryToCampaign(beneficiaryId = beneficiaryId,
                campaignId = campaignId)
            if (response.status == API_SUCCESS && response.code in 200..201) {
                _uiState.postValue(ExistingBeneficiaryUiState.Success(message = response.message))
            } else {
                _uiState.postValue(ExistingBeneficiaryUiState.Error(errorMessage = response.message))
            }
        }
    }

    sealed class ExistingBeneficiaryUiState {
        object Loading : ExistingBeneficiaryUiState()
        data class Error(val errorMessage: String?) : ExistingBeneficiaryUiState()
        data class Success(val message: String) : ExistingBeneficiaryUiState()
    }
}
