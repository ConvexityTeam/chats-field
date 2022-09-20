package com.codose.chats.views.cashForWork

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.network.NetworkRepository
import com.codose.chats.utils.BluetoothConstants.API_SUCCESS
import com.codose.chats.utils.handleThrowable
import com.codose.chats.views.cashForWork.model.TaskDetailsResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class TaskDetailsViewModel(private val repository: NetworkRepository) : ViewModel() {

    private val _uiState = MutableLiveData<TaskDetailsUiState>()
    val uiState: LiveData<TaskDetailsUiState> = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = TaskDetailsUiState.Error(throwable.handleThrowable())
    }

    fun getTaskDetails(taskId: String) {
        _uiState.value = TaskDetailsUiState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = repository.getTasksDetails(taskId)
            if (response.code in 200..202 && response.status == API_SUCCESS) {
                response.data?.let {
                    Timber.d("Response: ${response.data}")
                    _uiState.postValue(TaskDetailsUiState.Success(it))
                }
            } else {
                _uiState.postValue(TaskDetailsUiState.Error(response.message))
            }
        }
    }

    sealed class TaskDetailsUiState {
        object Loading : TaskDetailsUiState()
        data class Success(val task: TaskDetailsResponse) : TaskDetailsUiState()
        data class Error(val errorMessage: String?) : TaskDetailsUiState()
    }
}
