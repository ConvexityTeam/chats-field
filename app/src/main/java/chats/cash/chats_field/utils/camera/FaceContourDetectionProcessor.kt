package chats.cash.chats_field.utils.camera

import android.graphics.Rect
import androidx.camera.core.ExperimentalGetImage
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import timber.log.Timber
import java.io.IOException

@ExperimentalGetImage class FaceContourDetectionProcessor(
    private val view: GraphicOverlay,
    val onDetected: (List<Face>) -> Unit,
) :
    BaseImageAnalyzer<List<Face>>() {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setContourMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private val highAccuracyOpts: FaceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(highAccuracyOpts)

    override val graphicOverlay: GraphicOverlay
        get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Timber.e("Exception thrown while trying to close Face Detector: " + e)
        }
    }

    override fun onSuccess(
        results: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect,
    ) {
        graphicOverlay.clear()
        Timber.d(results.size.toString())
        results.forEach {
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        onDetected(results)
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Timber.tag(TAG).w("Face Detector failed." + e)
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}
