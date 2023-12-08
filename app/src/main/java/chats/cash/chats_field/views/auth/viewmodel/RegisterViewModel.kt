package chats.cash.chats_field.views.auth.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.imageServices.ImageServiceProvider
import chats.cash.chats_field.network.api.interfaces.BeneficiaryRepositoryInterface
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryGroupDetails
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryMember
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.repository.auth.AuthRepositoryInterface
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.vendor.VendorOnboardingResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineRepository
import com.eyecool.iris.api.IrisSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.nio.charset.StandardCharsets

class RegisterViewModel(
    private val offlineRepository: OfflineRepository,
    private val authRepository: AuthRepositoryInterface,
    private val imageProvider: ImageServiceProvider,
    private val beneficiaryRepository: BeneficiaryRepositoryInterface,
) : ViewModel() {

    var profileImage: String? = null
    var tempBeneficiary: Beneficiary? = null
    var nfc: String? = null
    val login = MutableLiveData<NetworkResponse<LoginResponse>>()
    val forgot = MutableLiveData<NetworkResponse<ForgotPasswordResponse>>()

    val userProfile = authRepository.getUserProfile()

    private var _fingerPrintScanProgress = 0
    val fingerPrintScanProgress: Int
        get() = _fingerPrintScanProgress

    private val _fingerPrintsScanned = MutableStateFlow<MutableList<Bitmap>>(mutableListOf())
    val fingerPrintsScanned: StateFlow<List<Bitmap>>
        get() = _fingerPrintsScanned

    fun restartFingerPrintScan() {
        _fingerPrintScanProgress = 0
        _fingerPrintsScanned.value = mutableListOf()
    }

    fun savFingerPrint(bitmap: Bitmap, index: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            val list = _fingerPrintsScanned.value
            if (list != null) {
                list.add(index, bitmap)
                _fingerPrintsScanned.value = list
                _fingerPrintScanProgress += 1
                onSuccess()
            } else {
                onError()
            }
        }
    }

    init {
        getAllCampaignForms()
        getAllCampaigns2()
    }

    fun getAllCampaignForms() = viewModelScope.async {
        val allForms = beneficiaryRepository.getAllCampaignForms()
        allForms.collect {
            Timber.v(it.toString())
            if (it is NetworkResponse.Success) {
                Timber.v(it.body.toString())
            }
        }
    }

    fun uploadImage(file: String, name: String) = viewModelScope.async {
        Timber.v("uploading image")
        imageProvider.uploadImage(file, name, true) {
        }.await()
    }

    private val _getAllCampaignState = MutableLiveData(GetAllCampaignState())
    val getAllCampaignState: LiveData<GetAllCampaignState>
        get() = _getAllCampaignState

    fun getAllCampaigns2() = viewModelScope.async {
        beneficiaryRepository.getAllCampaigns().collect {
            val loading = it is NetworkResponse.Loading
            val success = it is NetworkResponse.Success
            val error = it is NetworkResponse.Error || it is NetworkResponse.SimpleError
            val errorMessage = if (it is NetworkResponse.Error) {
                it._message
            } else if (it is NetworkResponse.SimpleError) it._message else ""
            val data = if (it is NetworkResponse.Success) it.body else null
            withContext(Dispatchers.Main) {
                _getAllCampaignState.value =
                    GetAllCampaignState(loading, success, error, errorMessage, data)
            }
        }
    }

    var specialCase = false
    var nin = ""
    var campaign: ModelCampaign? = null

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

    fun loginNGO(loginBody: LoginBody) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.loginNGO(loginBody).collect { data ->
                    login.postValue(data)
                }
            }
        }
    }

    fun sendForgotEmail(email: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.sendForgotEmail(email).collect { data ->
                    forgot.postValue(data)
                }
            }
        }
    }

    private val _onboardBeneficiaryResponse = MutableStateFlow<NetworkResponse<String>?>(null)
    val onboardBeneficiaryResponse: StateFlow<NetworkResponse<String>?>
        get() = _onboardBeneficiaryResponse

    var isGroupBeneficiary = false

    fun onboardBeneficiary(
        beneficiary: Beneficiary,
        currentInternetAvailabilityStatus: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isGroupBeneficiary.not()) {
                beneficiaryRepository.OnboardBeneficiary(
                    beneficiary,
                    currentInternetAvailabilityStatus,
                )
                    .collect {
                        _onboardBeneficiaryResponse.emit(it)
                    }
            } else {
                val state = _registerBeneficiaryState.value
                if (state?.groupName.isNullOrEmpty() || state?.category.isNullOrEmpty()) {
                    Timber.v("group name or category is null ${_registerBeneficiaryState.value}")
                    return@launch
                }
                val members = _addedGroupBeneficiaries.value?.map {
                    GroupBeneficiaryMember(it.name, it.picture?.path ?: "", it.dob)
                }

                if (members.isNullOrEmpty()) {
                    Timber.v("members are null or empty ${_addedGroupBeneficiaries.value}")
                    return@launch
                }
                val groupDetails =
                    GroupBeneficiaryGroupDetails(
                        state?.groupName!!.trim(),
                        state.category?.lowercase()?.trim()!!,
                    )

                val body = GroupBeneficiaryBody(
                    beneficiary.firstName + beneficiary.lastName,
                    groupDetails,
                    members,
                    beneficiary.copy(isGroup = true),
                    beneficiary.campaignId.toInt(),
                )

                Timber.v("ONBOARDING $body")

                onboardGroupBeneficiary(
                    body,
                    currentInternetAvailabilityStatus,
                )
            }
        }
    }

    fun onboardGroupBeneficiary(
        body: GroupBeneficiaryBody,
        currentInternetAvailabilityStatus: Boolean,
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            beneficiaryRepository.OnboardGroupBeneficiary(
                body,
                currentInternetAvailabilityStatus,
            ).map {
                if (it is NetworkResponse.Success) {
                    NetworkResponse.Success(it.body.id.toString())
                } else {
                    it
                }
            }.collect {
                if (it is NetworkResponse.Success) {
                    _onboardBeneficiaryResponse.emit(NetworkResponse.Success(it.body as String))
                } else {
                    if (it is NetworkResponse.Error) {
                        _onboardBeneficiaryResponse.emit(
                            NetworkResponse.Error(
                                it._message,
                                it.e,
                            ),
                        )
                    } else if (it is NetworkResponse.SimpleError) {
                        _onboardBeneficiaryResponse.emit(NetworkResponse.SimpleError(it._message))
                    } else if (it is NetworkResponse.Offline) {
                        _onboardBeneficiaryResponse.emit(NetworkResponse.Offline())
                    } else {
                        _onboardBeneficiaryResponse.emit(NetworkResponse.Loading())
                    }
                }
            }
        }

    private var _iris: String? = null
    val iris: String?
        get() = _iris

    fun insertIrisSignature(byte: String?) {
        _iris = (byte)
    }

    //3000
    suspend fun irisDoesNotExist(): Boolean = viewModelScope.async {
        val userIris = (_iris)?.toByteArray(StandardCharsets.ISO_8859_1)
        val irisSdk = IrisSDK.getInstance()
        if (userIris != null) {
            val allIris = offlineRepository.getAllOrgnaizationBeneficiaries()
            Timber.v(allIris.toString())
            allIris.any {
                val byte = it.iris?.toByteArray(StandardCharsets.ISO_8859_1)
                val compare = irisSdk.compare(byte, userIris)
                Timber.v(compare.toString())
                compare > 2500
            }
        }
        return@async true
    }.await()

    private val _registerBeneficiaryState = MutableLiveData(RegisterBeneficiaryState())
    val registerBeneficiaryState: LiveData<RegisterBeneficiaryState>
        get() = _registerBeneficiaryState

    fun updateRegisterBeneficiaryState_Stage1(category: String, groupName: String) {
        viewModelScope.launch {
            _registerBeneficiaryState.value =
                _registerBeneficiaryState.value?.copy(category = category, groupName = groupName)
        }
    }

    fun updateBeneficiaryGroup(category: String) {
        viewModelScope.launch {
            _registerBeneficiaryState.value =
                _registerBeneficiaryState.value?.copy(category = category)
        }
    }

    private var currentEditingGroupBeneficiaryIndex = -1
    private var currentEditingGroupBeneficiary: BeneficiaryMembers? = null

    private val _addedGroupBeneficiaries = MutableLiveData(mutableListOf(BeneficiaryMembers()))
    val addedGroupBeneficiaries: LiveData<List<BeneficiaryMembers>>
        get() = _addedGroupBeneficiaries.map { it.toList() }

    fun addItemToAddedGroupBeneficiaries(beneficiary: BeneficiaryMembers, onDone: () -> Unit) {
        val copy = _addedGroupBeneficiaries.value
        copy?.add(beneficiary)
        _addedGroupBeneficiaries.value = (copy)
        onDone()
    }

    fun removeItemFromAddedGroupBeneficiaries(index: Int, onDone: () -> Unit) {
        if (index != -1) {
            val copy = _addedGroupBeneficiaries.value
            copy?.removeAt(index)
            _addedGroupBeneficiaries.postValue(copy)
            onDone()
        }
    }

    fun editItemFromAddedGroupBeneficiaries(
        beneficiary: BeneficiaryMembers,
        position: Int,
        onDone: () -> Unit,
    ) {
        if (position != -1) {
            val copy = _addedGroupBeneficiaries.value!!
            copy[position] = beneficiary
            _addedGroupBeneficiaries.postValue(copy)
            onDone()
        }
    }

    fun getCurrentEditingBeneficiaryDetails(): Pair<BeneficiaryMembers, Int>? {
        if (currentEditingGroupBeneficiaryIndex != -1 && currentEditingGroupBeneficiary != null) {
            val result = Pair(currentEditingGroupBeneficiary!!, currentEditingGroupBeneficiaryIndex)
            currentEditingGroupBeneficiaryIndex = -1
            currentEditingGroupBeneficiary = null
            return result
        }
        return null
    }

    var mcallBack: (File) -> Unit = {}
    fun initPropsForImageSelect(
        beneficiary: BeneficiaryMembers,
        index: Int,
        callBack: (File) -> Unit,
    ) {
        currentEditingGroupBeneficiaryIndex = index
        currentEditingGroupBeneficiary = beneficiary
        mcallBack = callBack
    }

    fun resetGroupDetails() {
        _addedGroupBeneficiaries.value = mutableListOf(BeneficiaryMembers())
    }

    fun resetOnboardState() = viewModelScope.launch {
        _onboardBeneficiaryResponse.emit(null)
    }

    fun saveForOffline(beneficiary: Beneficiary?) = viewModelScope.launch {
        beneficiary?.let { offlineRepository.insert(it) }
    }
}

sealed class VendorOnboardingState {
    object Loading : VendorOnboardingState()
    object Success : VendorOnboardingState()
    data class Error(val errorMessage: String?) : VendorOnboardingState()
}

data class RegisterBeneficiaryState(
    val category: String? = null,
    val groupName: String? = null,
    val isGroup: Boolean = false,
    val members: List<BeneficiaryMembers> = emptyList(),
)

data class BeneficiaryMembers(
    val name: String = "",
    val dob: String = "",
    val picture: File? = null,
    val isedit: Boolean = true,
)

data class GetAllCampaignState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: Boolean = false,
    val errorMessage: String = "",
    val data: List<ModelCampaign>? = null,
)
