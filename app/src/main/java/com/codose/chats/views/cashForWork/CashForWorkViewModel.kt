package com.codose.chats.views.cashForWork

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.NetworkRepository
import com.codose.chats.network.response.campaign.CampaignByOrganizationModel
import com.codose.chats.network.response.progress.SubmitProgressModel
import com.codose.chats.network.response.tasks.GetTasksModel
import com.codose.chats.network.response.tasks.details.TaskDetailsModel
import com.codose.chats.utils.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.io.File

class CashForWorkViewModel(private val networkRepository: NetworkRepository) : ViewModel() {
    val cashForWorks = MutableLiveData<ApiResponse<CampaignByOrganizationModel>>()
    val cashForWorkCampaign = MutableLiveData<List<ModelCampaign>>()
    val tasks = MutableLiveData<ApiResponse<GetTasksModel>>()
    val taskDetails = MutableLiveData<ApiResponse<TaskDetailsModel>>()
    val taskOperation = MutableLiveData<ApiResponse<SubmitProgressModel>>()
    val imageList = MutableLiveData<ArrayList<Bitmap>>(arrayListOf())

    fun getCashForWorks(organizationId: String) {
        cashForWorks.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = networkRepository.getAllCashForWorkCampaigns()
                cashForWorkCampaign.postValue(data)
                //cashForWorks.postValue()
            }
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

    fun getTaskDetails(taskId: String) {
        taskDetails.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = networkRepository.getTasksDetails(taskId)
                taskDetails.postValue(data)
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