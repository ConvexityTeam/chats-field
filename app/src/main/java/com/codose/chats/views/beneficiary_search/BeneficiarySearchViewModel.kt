package com.codose.chats.views.beneficiary_search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.network.api.ConvexityApiService
import com.codose.chats.network.response.beneficiary_onboarding.Beneficiary
import com.codose.chats.utils.BluetoothConstants.API_SUCCESS
import com.codose.chats.utils.toDateString
import com.codose.chats.utils.toTitleCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BeneficiarySearchViewModel(private val service: ConvexityApiService) : ViewModel() {

    private val _uiState = MutableLiveData<BeneficiarySearchUiState>()
    val uiState: LiveData<BeneficiarySearchUiState> = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = BeneficiarySearchUiState.Error("Couldn't load beneficiaries, try again")
        Timber.e(throwable)
    }

    fun loadBeneficiaries(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        phone: String? = null,
        nin: String? = null
    ) {
        _uiState.value = BeneficiarySearchUiState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = withContext(Dispatchers.IO) {
                service.getExistingBeneficiary(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    nin = nin,
                    phone = phone)
            }
            if (response.status == API_SUCCESS && response.code in 200..202) {
                val beneficiaries = response.data
                if (beneficiaries.isNotEmpty()) {
                    _uiState.postValue(BeneficiarySearchUiState.Success(beneficiaries.map { it.mapToUi() }))
                } else {
                    _uiState.postValue(BeneficiarySearchUiState.EmptyBeneficiaries)
                }
            } else {
                _uiState.value = BeneficiarySearchUiState.Error(response.message)
            }
        }
    }

    private fun Beneficiary.mapToUi(): BeneficiaryUi {
        return BeneficiaryUi(
            dob = dob?.toDateString(),
            email = email,
            firstName = firstName?.trim(),
            gender = gender.toTitleCase(),
            id = id,
            lastName = lastName?.trim(),
            maritalStatus = maritalStatus,
            phone = phone,
            profilePic = profilePic
        )
    }

    sealed class BeneficiarySearchUiState {
        object Loading : BeneficiarySearchUiState()
        object EmptyBeneficiaries : BeneficiarySearchUiState()
        data class Error(val errorMessage: String?) : BeneficiarySearchUiState()
        data class Success(val beneficiaries: List<BeneficiaryUi>) : BeneficiarySearchUiState()
    }
}
