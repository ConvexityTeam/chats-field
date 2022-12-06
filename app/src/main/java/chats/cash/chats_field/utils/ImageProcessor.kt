package chats.cash.chats_field.utils

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import timber.log.Timber

class ImageProcessor(
    private val detector: FaceDetector,
    private val onSuccess: (List<Face>) -> Unit,
    private val onFailure: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            Timber.v("New Frame : ${image.format}")
            detector.process(image)
                .addOnSuccessListener { faces ->
                    onSuccess.invoke(faces)
                }
                .addOnFailureListener {
                    Timber.e(it)
                    onFailure.invoke(it)
                }
                .addOnCompleteListener {
                    mediaImage.close()
                    imageProxy.close()
                    Timber.v("Image Closed")
                }
        }
    }
}
