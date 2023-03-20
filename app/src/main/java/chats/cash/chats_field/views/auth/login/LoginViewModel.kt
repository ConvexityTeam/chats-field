package chats.cash.chats_field.views.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.response.login.Data
import chats.cash.chats_field.utils.ApiResponse
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(private val repository: NetworkRepository) : ViewModel() {

    private val _uiState = MutableLiveData<LoginState>()
    val uiState: LiveData<LoginState> = _uiState

    fun login(loginBody: LoginBody) {
        _uiState.value = LoginState.Loading
        viewModelScope.launch {
            when (val response = repository.loginNGO(loginBody)) {
                is ApiResponse.Failure -> _uiState.postValue(LoginState.Error(response.message))
                is ApiResponse.Loading -> _uiState.postValue(LoginState.Loading)
                is ApiResponse.Success -> _uiState.postValue(LoginState.Success(response.data.data))
            }
        }
    }



    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val result: Data) : LoginState()
        data class Error(val errorMessage: String?) : LoginState()
    }
}
