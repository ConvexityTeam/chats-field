package chats.cash.chats_field.utils

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ProgressRequestBody(file: File, private val content_type: String, private val mListener : ImageUploadCallback) : RequestBody() {
    private val mFile: File = file
    private val mPath: String? = null
    private val upload: String? = null



    @Nullable
    override fun contentType(): MediaType? {
        return "$content_type/*".toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return mFile.length()
    }

    @Throws(IOException::class)
    override fun writeTo(@NonNull sink: BufferedSink) {
        val fileLength: Long = mFile.length()
        val buffer =
            ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0
        FileInputStream(mFile).use { `in` ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (`in`.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                handler.post(ProgressUpdater(uploaded, fileLength))
            }
        }
    }

    private inner class ProgressUpdater internal constructor(
        private val mUploaded: Long,
        private val mTotal: Long
    ) :
        Runnable {

        override fun run() {
            if(mUploaded == 0L){
                mListener.onProgressUpdate(0)
            }else{
                mListener.onProgressUpdate((100 * mUploaded / mTotal).toInt())
            }
            ; //updating the UI of the progress
        }

    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }

}

interface ImageUploadCallback {
    fun onProgressUpdate(percentage: Int)
//    fun onError(message: String?)
//    fun onSuccess(message: String?)
}