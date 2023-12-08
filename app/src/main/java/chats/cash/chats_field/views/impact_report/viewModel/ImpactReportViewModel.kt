package chats.cash.chats_field.views.impact_report.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.videoServices.VideoServiceProvider
import chats.cash.chats_field.network.body.impact_report.ImpactReportBody
import chats.cash.chats_field.network.repository.impact_report.ImpactReport
import chats.cash.chats_field.network.response.impact_report.ImpactReportResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class ImpactReportViewModel(
    private val videoServiceProvider: VideoServiceProvider,
    private val impactReport: ImpactReport,
) : ViewModel() {

    private val _progress = MutableSharedFlow<Int>()
    val progress: SharedFlow<Int>
        get() = _progress

    private val _uploadState = MutableSharedFlow<UploadState>()
    val uploadState: SharedFlow<UploadState>
        get() = _uploadState

    fun uploadVideo(
        file: File,
        campaign: ModelCampaign,
        onProgress: (Int, String, String) -> Unit,
    ) = viewModelScope.launch {
        videoServiceProvider.uploadVideo(
            file.absolutePath,
            "impact_report_${campaign.title}_${Calendar.getInstance().time.time}",
            onNewState = { state ->
                viewModelScope.async {
                    _uploadState.emit(state)
                }
            },
        ) { progress, current, total ->
            viewModelScope.async {
                onProgress(progress, current, total)
                _progress.emit(progress)
            }
        }
    }

    private val _response = MutableSharedFlow<ImpactReportState>()
    val response: SharedFlow<ImpactReportState>
        get() = _response

    fun sendImpactReport(videoLink: String, title: String, fieldId: String, campaignid: Int) {
        viewModelScope.launch {
            val body = ImpactReportBody(fieldId, campaignid, videoLink, title)
            impactReport.sendImpactReport(body).collect {
                val loading = it is NetworkResponse.Loading
                val success = it is NetworkResponse.Success
                val error = it is NetworkResponse.Error
                val data = if (it is NetworkResponse.Success) it.body else null

                _response.emit(ImpactReportState(loading, error, success, data))
            }
        }
    }

    fun cancelUpload() = viewModelScope.launch {
        videoServiceProvider.cancelUpload().let {
            if (it) {
                _uploadState.emit(UploadState.Idle)
                _progress.emit(0)
            }
        }
    }
}

sealed interface UploadState {
    object Idle : UploadState
    object STARTED : UploadState
    object UPLOADING : UploadState
    data class COMPLETED(val url: String) : UploadState
    data class ERROR(val message: String) : UploadState
}

data class ImpactReportState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false,
    val data: ImpactReportResponse? = null,
)

//when(state){
//    is UploadState.COMPLETED -> {
//
//    }
//    is UploadState.ERROR -> TODO()
//    UploadState.STARTED -> TODO()
//    UploadState.UPLOADING -> TODO()
//}
