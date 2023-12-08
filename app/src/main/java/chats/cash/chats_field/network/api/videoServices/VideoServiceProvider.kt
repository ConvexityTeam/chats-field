package chats.cash.chats_field.network.api.videoServices

import chats.cash.chats_field.views.impact_report.viewModel.UploadState

interface VideoServiceProvider {

    suspend fun uploadVideo(
        path: String,
        fileName: String,
        onNewState: (UploadState) -> Unit,
        onProgress: (Int, String, String) -> Unit,
    )

    suspend fun cancelUpload(): Boolean
}
