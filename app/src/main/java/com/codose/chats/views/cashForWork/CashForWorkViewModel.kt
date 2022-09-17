package com.codose.chats.views.cashForWork

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.NetworkRepository
import com.codose.chats.network.response.campaign.CampaignByOrganizationModel
import com.codose.chats.network.response.progress.SubmitProgressModel
import com.codose.chats.network.response.tasks.GetTasksModel
import com.codose.chats.utils.ApiResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class CashForWorkViewModel(private val networkRepository: NetworkRepository) : ViewModel() {
    val cashForWorks = MutableLiveData<ApiResponse<CampaignByOrganizationModel>>()
    val tasks = MutableLiveData<ApiResponse<GetTasksModel>>()
    val taskOperation = MutableLiveData<ApiResponse<SubmitProgressModel>>()
    val imageList = MutableLiveData<ArrayList<Bitmap>>(arrayListOf())

    private val _cashForWorkCampaign = MutableLiveData<List<ModelCampaign>>()
    val cashForWorkCampaign: LiveData<List<ModelCampaign>> = _cashForWorkCampaign

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    fun getCashForWorks() {
        cashForWorks.value = ApiResponse.Loading()
        viewModelScope.launch(exceptionHandler) {
            val data = withContext(Dispatchers.IO) {
                networkRepository.getAllCashForWorkCampaigns()
            }
            _cashForWorkCampaign.value = data
        }
    }

    fun getTask(campaignId: String) {
        tasks.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = networkRepository.getTasks(campaignId)
                tasks.postValue(data)
            }
        }
    }

    fun postTaskImages(
        taskId: String,
        userId: String,
        description: String,
        images: ArrayList<File>,
    ) {
        taskOperation.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = networkRepository.postTaskEvidence(taskId, userId, description, images)
                taskOperation.postValue(data)
            }
        }
    }

    fun postTaskCompleted(taskId: String, userId: String) {
        taskOperation.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = networkRepository.postTaskCompleted(taskId, userId)
                taskOperation.postValue(data)
            }
        }
    }
}
