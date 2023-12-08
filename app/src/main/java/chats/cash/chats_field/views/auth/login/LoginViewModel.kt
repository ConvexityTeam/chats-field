package chats.cash.chats_field.views.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.repository.auth.AuthRepositoryInterface
import chats.cash.chats_field.network.response.login.Data
import chats.cash.chats_field.network.response.login.User
import chats.cash.chats_field.offline.OfflineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepositoryInterface,
    private val offlineRepository: OfflineRepository,
) : ViewModel() {

    private val _uiState = MutableSharedFlow<LoginState>()
    val uiState: SharedFlow<LoginState> = _uiState

    val getBeneficiaries = offlineRepository.getAllBeneficiary()
    val getGroupBeneficiaries = offlineRepository.getAllGroupBeneficiary()

    fun login(loginBody: LoginBody) = viewModelScope.launch {
        repository.loginNGO(loginBody).collect { result ->
            when (result) {
                is NetworkResponse.Error -> {
                    _uiState.emit(LoginState.Error(result._message))
                }

                is NetworkResponse.SimpleError -> _uiState.emit(
                    LoginState.Error(result._message),
                )

                is NetworkResponse.Loading -> _uiState.emit(LoginState.Loading)
                is NetworkResponse.Success -> {
                    _uiState.emit(LoginState.Success(result.body.data))
                }

                else -> {
                }
            }
        }
    }

    fun getUserProfile(): User? {
        return repository.getUserProfile()
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                repository.logout()
                offlineRepository.deleteAllTables()
            }
        }
    }

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val result: Data) : LoginState()
        data class Error(val errorMessage: String?) : LoginState()
    }
}
