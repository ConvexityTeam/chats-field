package chats.cash.chats_field.network.api.videoServices

import android.content.Context
import chats.cash.chats_field.views.impact_report.viewModel.UploadState
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import timber.log.Timber

class VideoServiceProviderImpl(private val context: Context) : VideoServiceProvider {

    var currentRequestId: String? = null

    override suspend fun uploadVideo(
        path: String,
        fileName: String,
        onNewState: (UploadState) -> Unit,
        onProgress: (Int, String, String) -> Unit,
    ) {
        currentRequestId = null
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "convexity"
        try {
            MediaManager.init(context, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val uploadCallback = object : UploadCallback {
            override fun onStart(requestId: String) {
                // Upload started
                Timber.d("on start ")
                onNewState(UploadState.STARTED)
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                // Upload progress )
                val progress = (bytes * 100 / totalBytes).toInt()
                val _totalBytes = totalBytes / (1024.0 * 1024.0) // Total progress in MB
                val transferredBytes = bytes / (1024.0 * 1024.0) // Current progress in MB

                val formattedTotalProgress = String.format("%.2f", _totalBytes)
                val formattedCurrentProgress = String.format("%.2f", transferredBytes)
                Timber.d("Upload $progress $formattedTotalProgress $formattedCurrentProgress")
                onNewState(UploadState.UPLOADING)
                onProgress(progress, formattedCurrentProgress, formattedTotalProgress)
                // Update your UI with the upload progress
            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                // Upload successful
                val publicId = resultData["public_id"] as String
                val secureUrl = resultData["secure_url"] as String
                val url = resultData["url"] as String
                onNewState(UploadState.COMPLETED(url))
                // Handle the uploaded video details, e.g., save the secure URL
                // Update your UI with the secure URL or perform any additional tasks
                Timber.d("On Success ${resultData.keys} ${resultData.values}")
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                // Upload error
                val description = error.description
                // Handle the upload error
                onNewState(UploadState.ERROR(description))
                Timber.d("On error $description")
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {
                // Upload rescheduled
                // Handle the rescheduled upload
                onNewState(UploadState.ERROR("Upload rescheduled"))
            }
        }

        val requestId = MediaManager.get().upload(path)
            .unsigned("dhhdapk5")
            .option("resource_type", "video")
            .option("folder", "CHATS_impact_report")
            .option("public_id", fileName)
            .callback(uploadCallback)
            .dispatch()

        currentRequestId = requestId
    }

    override suspend fun cancelUpload(): Boolean {
        return MediaManager.get().cancelRequest(currentRequestId)
    }
}
