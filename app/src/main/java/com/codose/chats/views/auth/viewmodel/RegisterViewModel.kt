package com.codose.chats.views.auth.viewmodel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.model.ModelCampaign
import com.codose.chats.model.NFCModel
import com.codose.chats.network.NetworkRepository
import com.codose.chats.network.body.login.LoginBody
import com.codose.chats.network.response.NfcUpdateResponse
import com.codose.chats.network.response.RegisterResponse
import com.codose.chats.network.response.UserDetailsResponse
import com.codose.chats.network.response.campaign.GetAllCampaignsResponse
import com.codose.chats.network.response.forgot.ForgotPasswordResponse
import com.codose.chats.network.response.login.LoginResponse
import com.codose.chats.network.response.organization.OrganizationResponse
import com.codose.chats.offline.OfflineRepository
import com.codose.chats.utils.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File

class RegisterViewModel(
    private val repository: NetworkRepository,
    offlineRepository: OfflineRepository,
) : ViewModel() {
    val getCampaigns = offlineRepository.getAllCampaigns()
    var allFinger: ArrayList<Bitmap>? = null
    var profileImage: String? = null
    var nfc: String? = null
    val onboardUser = MutableLiveData<ApiResponse<RegisterResponse>>()
    val vendorOnboarding = MutableLiveData<ApiResponse<RegisterResponse>>()
    val ngos = MutableLiveData<ApiResponse<OrganizationResponse>>()
    val orgaizations = MutableLiveData<ApiResponse<List<ModelCampaign >>>()
    val login = MutableLiveData<ApiResponse<LoginResponse>>()
    val userDetails = MutableLiveData<ApiResponse<UserDetailsResponse>>()
    val nfcDetails = MutableLiveData<ApiResponse<NfcUpdateResponse>>()
    val forgot = MutableLiveData<ApiResponse<ForgotPasswordResponse>>()

    var specialCase = false
    var nin = "";
    var campaign: String = "1"
    fun onboardUser(
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
    ) {
        onboardUser.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.onboardUser(firstName,
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
                    campaign)
                onboardUser.postValue(data)
            }
        }
    }

    fun onboardSpecialUser(
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
        nin: RequestBody,
    ) {
        onboardUser.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.onboardSpecialUser(
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
                    nin,
                )
                onboardUser.postValue(data)
            }
        }
    }

    fun vendorOnboarding(
        businessName: String,
        email: String,
        phone: String,
        password: String,
        pin: String,
        bvn: String,
        firstName: String,
        lastName: String
    ) {
        onboardUser.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data =
                    repository.vendorOnboarding(businessName, email, phone, password, pin, bvn, firstName, lastName)
                onboardUser.postValue(data)
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

    fun getCampaigns() {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.getCampaigns()

            }
        }
    }

    fun getOrganizationCampaigns(){
        Timber.v("getOrganizationsCalled")
        orgaizations.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val data = repository.getAllCampaigns()
                Timber.v("All data"+data.toString())
                //orgaizations.value = ApiResponse.Success(data)
                //getCampaigns.postValue(data)
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
}