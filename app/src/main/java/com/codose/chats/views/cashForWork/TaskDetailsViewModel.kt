package com.codose.chats.views.cashForWork

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.network.NetworkRepository
import com.codose.chats.network.response.tasks.details.Task
import com.codose.chats.utils.BluetoothConstants.API_SUCCESS
import com.codose.chats.utils.handleThrowable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class TaskDetailsViewModel(private val repository: NetworkRepository) : ViewModel() {

    private val _uiState = MutableLiveData<TaskDetailsUiState>()
    val uiState: LiveData<TaskDetailsUiState> get() = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = TaskDetailsUiState.Error(throwable.handleThrowable())
    }

    fun getTaskDetails(taskId: String) {
        viewModelScope.launch(exceptionHandler) {
            val response = repository.getTasksDetails(taskId)
            if (response.code in 200..202 && response.status == API_SUCCESS) {
                response.data?.let {
                    _uiState.postValue(TaskDetailsUiState.Success(it.data.task))
                }
            } else {
                _uiState.postValue(TaskDetailsUiState.Error(response.message))
            }
        }
    }

    sealed class TaskDetailsUiState {
        object Loading : TaskDetailsUiState()
        data class Success(val task: Task) : TaskDetailsUiState()
        data class Error(val errorMessage: String?) : TaskDetailsUiState()
    }
}