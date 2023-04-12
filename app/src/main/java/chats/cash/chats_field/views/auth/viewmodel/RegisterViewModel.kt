package chats.cash.chats_field.views.auth.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.NFCModel
import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.repository.BeneficiaryRepository
import chats.cash.chats_field.network.response.NfcUpdateResponse
import chats.cash.chats_field.network.response.RegisterResponse
import chats.cash.chats_field.network.response.UserDetailsResponse
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.organization.OrganizationResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineRepository
import chats.cash.chats_field.utils.ApiResponse
import chats.cash.chats_field.utils.handleThrowable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File

class RegisterViewModel(
    private val repository: NetworkRepository,
    private val beneficiaryRepository: BeneficiaryRepository,
) : ViewModel() {
    var allFinger: ArrayList<Bitmap>? = null
    var profileImage: String? = null
    var nfc: String? = null
    val ngos = MutableLiveData<ApiResponse<OrganizationResponse>>()
    val organizations = MutableLiveData<ApiResponse<List<ModelCampaign>>>()
    val login = MutableLiveData<ApiResponse<LoginResponse>>()
    val userDetails = MutableLiveData<ApiResponse<UserDetailsResponse>>()
    val nfcDetails = MutableLiveData<ApiResponse<NfcUpdateResponse>>()
    val forgot = MutableLiveData<ApiResponse<ForgotPasswordResponse>>()


    init {
        viewModelScope.launch {
            getAllCampaignForms()
            getAllCampaigns2()
        }
    }

    fun getAllCampaignForms() = viewModelScope.launch {
        val allForms = beneficiaryRepository.getAllCampaignForms()
        allForms.collect {
            Timber.v(it.toString())
            if (it is NetworkResponse.Success) {
                Timber.v(it.body.toString())
            }
        }
    }

    suspend fun getAllCampaigns2() = viewModelScope.launch {
        beneficiaryRepository.getAllCampaigns().collect {
            Timber.v(it.toString())
        }
    }

    var specialCase = false
    var nin = ""
    var campaign: String = "1"

    private val _onboardVendorResponse =
        Channel<NetworkResponse<VendorOnboardingResponse.VendorResponseData>>()
    val onboardVendorResponse: Flow<NetworkResponse<VendorOnboardingResponse.VendorResponseData>>
        get() = _onboardVendorResponse.receiveAsFlow()

    fun vendorOnboarding(
        beneficiary: Beneficiary,
        isOnline: Boolean,
    ) = viewModelScope.launch {

        beneficiaryRepository.OnboardVendor(beneficiary, isOnline).collect {
            _onboardVendorResponse.send(it)
        }
    }

    fun getNGOs() {
        ngos.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.getNGOs()
                ngos.postValue(data)
            }
        }
    }


    fun getUserDetails(id: String) {
        userDetails.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.getUserDetails(id)
                userDetails.postValue(data)
            }
        }
    }

    fun postNFCDetails(nfcModel: NFCModel) {
        nfcDetails.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.postNFCDetails(nfcModel)
                nfcDetails.postValue(data)
            }
        }
    }

    fun loginNGO(loginBody: LoginBody) {
        login.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.loginNGO(loginBody)
                login.postValue(data)
            }
        }
    }

    fun sendForgotEmail(email: String) {
        forgot.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.sendForgotEmail(email)
                forgot.postValue(data)
            }
        }
    }

    private val _onboardBeneficiaryResponse = Channel<NetworkResponse<String>>()
    val onboardBeneficiaryResponse: Flow<NetworkResponse<String>>
        get() = _onboardBeneficiaryResponse.receiveAsFlow()

    fun onboardBeneficiary(
        beneficiary: Beneficiary,
        currentInternetAvailabilityStatus: Boolean,
    ) = viewModelScope.launch(Dispatchers.IO) {
        beneficiaryRepository.OnboardBeneficiary(beneficiary, currentInternetAvailabilityStatus)
            .collect {
                _onboardBeneficiaryResponse.send(it)
            }
    }

    sealed class VendorOnboardingState {
        object Loading : VendorOnboardingState()
        object Success : VendorOnboardingState()
        data class Error(val errorMessage: String?) : VendorOnboardingState()
    }
}
