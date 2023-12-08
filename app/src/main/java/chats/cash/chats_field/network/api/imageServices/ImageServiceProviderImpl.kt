package chats.cash.chats_field.network.api.imageServices

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import chats.cash.chats_field.network.api.ConvexityApiService
import chats.cash.chats_field.utils.PreferenceUtilInterface
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageServiceProviderImpl(
    private val context: Context,
    private val convexityApiService: ConvexityApiService,
    private val preferenceUtil: PreferenceUtilInterface,
) : ImageServiceProvider {

    override suspend fun uploadImage(
        path: String,
        fileName: String,
        shouldCompressImage: Boolean,
        onProgress: (Float) -> Unit,
    ): Deferred<String?> {
        Timber.v("starting")
        val result = CompletableDeferred<String?>()
        withContext(Dispatchers.IO) {
            try {
                val imageFile = File(path)

                if (imageFile.exists()
                ) {
                    val imageToUpload =
                        if (shouldCompressImage) compressImage(imageFile, fileName) else imageFile

                    if (imageToUpload != null) {
                        val mBody =
                            imageToUpload.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                        val image = MultipartBody.Part.createFormData(
                            "profile_pic",
                            "$fileName.jpg",
                            mBody,
                        )
                        val response = convexityApiService.UploadImage(image, preferenceUtil.getNGOToken())

                        if (response.code.toString().startsWith("2")) {
                            imageFile.delete()
                            imageToUpload.delete()
                            result.complete(response.link)
                            return@withContext response.link
                        } else {
                            return@withContext null
                        }

//
//                            override fun onProgressChanged(
//                                id: Int,
//                                bytesCurrent: Long,
//                                bytesTotal: Long,
//                            ) {
//                                onProgress((bytesCurrent / bytesTotal) * 100f)
//                                Timber.d("progress ${((bytesCurrent / bytesTotal) * 100f)}")
//                                if (bytesCurrent == bytesTotal) {
//                                    Timber.d("done ")
//                                }
//                            }
                    } else {
                        result.complete(null)
                        Timber.v("compressed null")
                    }
                } else {
                    result.complete(null)
                    Timber.v("image not exists")
                }
            } catch (e: Exception) {
                Timber.v("error")
                e.printStackTrace()
                result.complete(null)
            }
        }
        return result
    }

    private fun compressImage(file: File, name: String): File? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val compressedData = outputStream.toByteArray()

            val outputDir = context.cacheDir

            if (outputDir != null) {
                val outputFile = File.createTempFile(name, ".jpg", context.filesDir)
                try {
                    val outputStream2 = FileOutputStream(outputFile)
                    outputStream2.write(compressedData)
                    outputStream2.close()

                    // File saved successfully, do further processing or display a success message
                    return outputFile
                } catch (e: IOException) {
                    e.printStackTrace()
                    // Handle the exception appropriately
                }
            }
            if (outputDir == null) {
                Timber.tag("COMPRESS").v("outpurdir is null ${context.cacheDir.absolutePath}")
            }
        }
        if (bitmap == null) {
            Timber.tag("COMPRESS").v("bitmap is null $file")
        }

        return null
    }
}
