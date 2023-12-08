package chats.cash.chats_field.views.faceCapture

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ActivityConvexityFaceCaptureBinding
import chats.cash.chats_field.utils.camera.CameraManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class ConvexityFaceCaptureActivityCaptureActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File

    private var photoUri: String = ""

    private lateinit var binding: ActivityConvexityFaceCaptureBinding

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    @SuppressLint("MissingPermission")
    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConvexityFaceCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, inset ->
            val insets = inset.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
        onViewCreated()
        WindowCompat.setDecorFitsSystemWindows(window, (false))
    }

    @SuppressLint("MissingPermission")
    @ExperimentalGetImage
    fun onViewCreated() {
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Determine the output directory
        outputDirectory = getOutputDirectory(this)

        // Wait for the views to be properly laid out
        binding.let {
            it.viewFinder.post {
                it.viewFinder.controller?.cameraInfo?.let { info ->
                    //   observeCameraState(info)
                }
                // Build UI controls
                updateCameraUi()

                cameraManager?.startCamera()
                // Set up the camera and its use cases
                // setUpCamera()
            }
        }
    }

    private var cameraManager: CameraManager? = null

    override fun onPause() {
        super.onPause()
        cameraManager = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @ExperimentalGetImage
    override fun onResume() {
        super.onResume()
        createCameraManager()
    }

    @ExperimentalGetImage
    private fun createCameraManager() {
        binding.apply {
            val screenAspectRatio =
                aspectRatio(root.width, root.height)

            cameraManager = CameraManager(
                this@ConvexityFaceCaptureActivityCaptureActivity,
                viewFinder,
                this@ConvexityFaceCaptureActivityCaptureActivity,
                graphicOverlayFinder,
                screenAspectRatio,
            ) { result, imagecaptures ->
                try {
                    imageCapture = imagecaptures

                    if (result.size > 1) {
                        this@ConvexityFaceCaptureActivityCaptureActivity.showToast(
                            getString(R.string.please_make_sure_you_are_the_only_one_in_frame),
                        )
                        cameraCaptureButton.isEnabled = false
                    } else {
                        cameraCaptureButton.isEnabled = !result.isEmpty()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = Integer.max(width, height).toDouble() / Integer.min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    @ExperimentalGetImage
    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {
        binding.apply {
            flipButton.setOnClickListener {
                cameraManager?.changeCameraSelector()
            }

            // Listener for button used to capture photo
            cameraCaptureButton.setOnClickListener {
                takePhoto()
            }
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        imageCapture?.let { imageCapture ->

            // Create output file to hold the image
            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Timber.e(exc, getString(R.string.photo_capture_failed) + exc.message)
                        showToast(R.string.photo_capture_failed)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri: Uri = output.savedUri ?: Uri.fromFile(photoFile)

                        try {
                            photoUri = savedUri.toString()

                            lifecycleScope.launch(Dispatchers.Main) {
                                binding.imagePreviewView?.loadGlide(photoFile)
                                try {
                                    binding.apply {
                                        previewGroup.show()
                                        captureGroup.hide()
                                        retakeButton.setOnClickListener {
                                            photoFile.delete()
                                            showCamera()
                                        }
                                        selectButton.setOnClickListener {
                                            val intent = Intent()
                                            intent.data = savedUri
                                            this@ConvexityFaceCaptureActivityCaptureActivity.setResult(RESULT_OK, intent)
                                            finish()
                                        }
                                    }
                                } catch (e: Exception) {
                                    this@ConvexityFaceCaptureActivityCaptureActivity.setResult(RESULT_CANCELED)
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                },
            )

            // We can only change the foreground Drawable using API level 23+ API

            // Display flash animation to indicate that photo was captured
            binding.root.postDelayed({
                binding.root.foreground = ColorDrawable(Color.WHITE)

                binding.root.postDelayed(
                    { binding.root.foreground = null },
                    ANIMATION_FAST_MILLIS,
                )
            }, ANIMATION_SLOW_MILLIS)
        }
    }

    private fun showCamera() = with(binding) {
        this.previewGroup.hide()
        this.captureGroup.show()
    }

    companion object {

        private const val TAG = "CameraFragment"
        const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Helper function used to create a timestamped file */
        fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder,
                SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension,
            )

        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists()) {
                mediaDir
            } else {
                appContext.filesDir
            }
        }
    }
}

private fun View.show() {
    visibility = View.VISIBLE
}

private fun View.hide() {
    visibility = View.GONE
}

private fun ConvexityFaceCaptureActivityCaptureActivity.showToast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
private fun ConvexityFaceCaptureActivityCaptureActivity.showToast(s: Int) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

fun ImageView.loadGlide(photoFile: File) {
    Glide.with(this).load(photoFile).into(this)
}

/** Milliseconds used for UI animations */
const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 100L
