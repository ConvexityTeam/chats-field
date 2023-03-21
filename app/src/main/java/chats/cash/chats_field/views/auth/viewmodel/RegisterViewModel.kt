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
import chats.cash.chats_field.offline.OfflineRepository
import chats.cash.chats_field.utils.ApiResponse
import chats.cash.chats_field.utils.handleThrowable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File

class RegisterViewModel(
    private val repository: NetworkRepository,
    private val beneficiaryRepository: BeneficiaryRepository,
    offlineRepository: OfflineRepository,
) : ViewModel() {
    var allFinger: ArrayList<Bitmap>? = null
    var profileImage: String? = null
    var nfc: String? = null
    val onboardUser = MutableLiveData<ApiResponse<RegisterResponse>>()
    val vendorOnboarding = MutableLiveData<ApiResponse<RegisterResponse>>()
    val ngos = MutableLiveData<ApiResponse<OrganizationResponse>>()
    val organizations = MutableLiveData<ApiResponse<List<ModelCampaign>>>()
    val login = MutableLiveData<ApiResponse<LoginResponse>>()
    val userDetails = MutableLiveData<ApiResponse<UserDetailsResponse>>()
    val nfcDetails = MutableLiveData<ApiResponse<NfcUpdateResponse>>()
    val forgot = MutableLiveData<ApiResponse<ForgotPasswordResponse>>()

    private val _vendorOnboardingUiState = MutableLiveData<VendorOnboardingState>()
    val vendorOnboardingState: LiveData<VendorOnboardingState> = _vendorOnboardingUiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _vendorOnboardingUiState.value = VendorOnboardingState.Error(throwable.handleThrowable())
    }

    init {
        viewModelScope.launch {
            getAllCampaignForms()
            getAllCampaigns2()
        }
    }

    fun getAllCampaignForms() = viewModelScope.launch{
        val allForms =  beneficiaryRepository.getAllCampaignForms()
        allForms.collect {
            Timber.v(it.toString())
            if(it is NetworkResponse.Success){
                Timber.v(it.body.toString())
            }
        }
    }

    fun getAllCampaigns2() = viewModelScope.launch{
       beneficiaryRepository.getAllCampaigns()
    }

    var specialCase = false
    var nin = ""
    var campaign: String = "1"

    fun onboardUser(
        organisationId: String,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        phone: RequestBody,
        password: RequestBody,
        lat: RequestBody,
        long: RequestBody,
        nfc: RequestBody,
        status: RequestBody,
        profile_pic: File,
        prints: ArrayList<MultipartBody.Part>,
        mGender: RequestBody,
        mDate: RequestBody,
        location: RequestBody,
        campaign: RequestBody,
        pin: RequestBody
    ) {
        onboardUser.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.onboardUser(
                    organisationId,
                    firstName,
                    lastName,
                    email,
                    phone,
                    password,
                    lat,
                    long,
                    nfc,
                    status,
                    profile_pic,
                    prints,
                    mGender,
                    mDate,
                    location,
                    campaign,
                    pin
                )
                if (data is ApiResponse.Success<RegisterResponse>) {
                    profile_pic.delete()
                }
                Timber.d("data: $data")
                onboardUser.postValue(data)
            }
        }
    }

    fun onboardSpecialUser(
        organisationId: String,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        phone: RequestBody,
        password: RequestBody,
        lat: RequestBody,
        long: RequestBody,
        nfc: RequestBody,
        status: RequestBody,
        profile_pic: File,
        mGender: RequestBody,
        mDate: RequestBody,
        location: RequestBody,
        campaign: RequestBody,
        pin: RequestBody,
        nin: String
    ) {
        onboardUser.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.onboardSpecialUser(
                    organisationId,
                    firstName,
                    lastName,
                    email,
                    phone,
                    password,
                    lat,
                    long,
                    nfc,
                    status,
                    profile_pic,
                    mGender,
                    mDate,
                    location,
                    campaign,
                    pin,
                    nin,
                )
                if (data is ApiResponse.Success<RegisterResponse>) {
                    profile_pic.delete()
                }
                Timber.d("data: $data")
                onboardUser.postValue(data)
            }
        }
    }

    fun vendorOnboarding(
        businessName: String,
        email: String,
        phone: String,
        firstName: String,
        lastName: String,
        address: String,
        country: String,
        state: String,
        coordinates: List<Double>
    ) {
        _vendorOnboardingUiState.value = VendorOnboardingState.Loading
        viewModelScope.launch(exceptionHandler) {
            withContext(Dispatchers.IO) {
                val data = repository.vendorOnboarding(
                    businessName = businessName,
                    email = email,
                    phone = phone,
                    firstName = firstName,
                    lastName = lastName,
                    address = address,
                    country = country,
                    state = state,
                    coordinates = coordinates
                )
                if (data.code == 201) {
                    _vendorOnboardingUiState.postValue(VendorOnboardingState.Success)
                } else {
                    _vendorOnboardingUiState.postValue(VendorOnboardingState.Error(data.message))
                }
            }
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


    /**
     * Method for getting all campaigns
     * */
    fun getAllCampaigns() {
        organizations.value = ApiResponse.Loading()
        viewModelScope.launch {
            val data = beneficiaryRepository.getAllCampaigns()
            organizations.postValue(organizations.value)
            Timber.v("All data: $data")
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

    sealed class VendorOnboardingState {
        object Loading : VendorOnboardingState()
        object Success : VendorOnboardingState()
        data class Error(val errorMessage: String?) : VendorOnboardingState()
    }
}
