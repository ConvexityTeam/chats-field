package chats.cash.chats_field.views.auth.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.net.toFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentImageCaptureBinding
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.camera.CameraManager
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import chats.cash.chats_field.views.core.showErrorSnackbar
import chats.cash.chats_field.views.core.showSuccessSnackbar
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Integer.max
import java.lang.Integer.min
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@ExperimentalGetImage class ImageCaptureFragment : BaseFragment() {

    private  var _binding: FragmentImageCaptureBinding?=null
    private val binding get() = _binding!!
    private var photoUri: String = ""

    private var cameraProgressState = MutableLiveData(CaptureProgressState.CLOSE_EYE)

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File


    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    private val viewModel by sharedViewModel<RegisterViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImageCaptureBinding.inflate(inflater,container,false)
        return binding?.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Determine the output directory
        outputDirectory = getOutputDirectory(requireContext())

        cameraProgressState.observe(viewLifecycleOwner){
            when(it){
                CaptureProgressState.CLOSE_EYE ->{
                    binding.progressbar.show()
                    binding.progressbar.max = 5
                    eyeCloseSteps.observe(viewLifecycleOwner){progressValue ->
                        if(progressValue!=null){
                            binding.progressbar.progress = progressValue
                        }
                    }
                }
                CaptureProgressState.BLINK -> {
                    binding.progressbar.hide()
                    binding.blinkCountText.text=getString(R.string.blink_eyes)
                    eyesClosed.observe(viewLifecycleOwner){
                        if(it>2 && eyesOpened.value!!>6){
                            if(cameraProgressState.value != CaptureProgressState.TAKE_PHOTO) {
                                cameraProgressState.value = CaptureProgressState.TAKE_PHOTO
                                eyesClosed.removeObservers(viewLifecycleOwner)
                            }
                        }
                    }
                    eyesOpened.observe(viewLifecycleOwner){
                        if(it>2 && eyesClosed.value!!>6){
                            if(cameraProgressState.value != CaptureProgressState.TAKE_PHOTO) {
                                cameraProgressState.value = CaptureProgressState.TAKE_PHOTO
                                eyesOpened.removeObservers(viewLifecycleOwner)
                            }
                        }
                    }
                }
                CaptureProgressState.TAKE_PHOTO -> {
                    binding.blinkCountText.text = getString(R.string.take_photo)
                    binding.cameraCaptureButton.setOnClickListener {
                        takePhoto()
                    }
                }
            }
        }

        // Wait for the views to be properly laid out
        binding.let {
            it.viewFinder.post {
                it.viewFinder.controller?.cameraInfo?.let {info ->
                observeCameraState(info)
                }
                // Build UI controls
                updateCameraUi()

                cameraManager?.startCamera()
                // Set up the camera and its use cases
                //setUpCamera()

            }
        }
    }
    private  var cameraManager: CameraManager?=null

    override fun onPause() {
        super.onPause()
        cameraManager=null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

    override fun onResume() {
        super.onResume()
        createCameraManager()
    }
    private fun createCameraManager() {

        binding.apply {
            val screenAspectRatio =
                aspectRatio(root.width, root.height)


            cameraManager = CameraManager(
                this@ImageCaptureFragment.requireContext(),
                viewFinder,
                this@ImageCaptureFragment,
                graphicOverlayFinder, screenAspectRatio
            ){result,imagecaptures ->
                if(_binding!=null) {
                    try {
                        imageCapture = imagecaptures

                        if (result.size > 1) {
                            binding.overlay.setCircleColor(requireContext().getColor(R.color.white))
                            this@ImageCaptureFragment.showToast("Please make sure you are the only one in frame")
                            cameraCaptureButton.isEnabled = false
                        } else if (result.isEmpty()) {
                            binding.overlay.setCircleColor(requireContext().getColor(R.color.white))
                            cameraCaptureButton.isEnabled = false
                        } else {
                            val face = result[0]

                            val radius = min(overlayView.width, overlayView.height) / 2f
                            val centerX = overlayView.x
                            val centerY = overlayView.y
                            if (face != null) {
                                val boundingBox = face.boundingBox
                                val top = face.boundingBox.top
                                val left = face.boundingBox.left
                                val boxCenterX = boundingBox.centerX().toFloat()
                                val boxCenterY = boundingBox.centerY().toFloat()
                                val distance =
                                    sqrt(
                                        (boxCenterX - centerX).pow(2) + (boxCenterY - centerY).pow(
                                            2
                                        )
                                    )
                                Timber.d(distance.toString())
                                Timber.d(boundingBox.top.toString())
                                Timber.d(boundingBox.left.toString())
                                Timber.d(binding.overlayView.top.toString())
                                Timber.d(binding.overlayView.left.toString())
                                if (distance <= radius && distance < 270 && top > 20 && left > 80) {
                                    binding.overlay.setCircleColor(requireContext().getColor(R.color.colorPrimary))
                                    Timber.d("IN FRAME")
                                    cameraProgressState.value.let {
                                        Timber.v(it?.name ?: "null")
                                        when (it) {
                                            CaptureProgressState.CLOSE_EYE -> {
                                                if (!shouldWait) {
                                                    lifecycleScope.launch { observeEyeClose(face) }
                                                }
                                            }
                                            CaptureProgressState.BLINK -> {
                                                if (!shouldWait) {
                                                    lifecycleScope.launch { observeEyeBlink(face) }
                                                }
                                            }
                                            CaptureProgressState.TAKE_PHOTO -> {
                                                cameraCaptureButton.isEnabled = true
                                            }
                                            else -> {}
                                        }
                                    }

                                } else {
                                    Timber.d("NOT IN FRAME")
                                    binding.overlay.setCircleColor(requireContext().getColor(R.color.white))
                                    cameraCaptureButton.isEnabled = false
                                }
                            }

                        }
                    }catch(e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private var eyeCloseSteps = MutableLiveData(0)
    private var shouldWait = false
    private suspend fun observeEyeClose(face: Face) {
        shouldWait = true
        while (eyeCloseSteps.value!!<5){
            val isRightEyeClosed = (face.rightEyeOpenProbability ?: 1f) < 0.5f
            val isLeftEyeClosed = (face.leftEyeOpenProbability ?: 1f) < 0.5f
            Timber.v("eye ${face.rightEyeOpenProbability} ${face.leftEyeOpenProbability}")
            if(isRightEyeClosed && isLeftEyeClosed){
                //increase step by 1
                eyeCloseSteps.value =  (eyeCloseSteps.value?:0)+1
                delay(1000)
                shouldWait=false
                return
            }
            else{
                delay(1000)
                shouldWait=false
                eyeCloseSteps.value = 0
                return
            }
        }
        shouldWait = false

        cameraProgressState.value = CaptureProgressState.BLINK

    }

    private var eyesClosed = MutableLiveData(0)
    private var eyesOpened = MutableLiveData(0)
    private suspend fun observeEyeBlink(face: Face) {
        shouldWait = true
            val isRightEyeClosed = (face.rightEyeOpenProbability ?: 1f) < 0.5f
            val isLeftEyeClosed = (face.leftEyeOpenProbability ?: 1f) < 0.5f
            Timber.v("eyeclosed ${face.rightEyeOpenProbability} ${face.leftEyeOpenProbability}")
            Timber.v("eyeclosed ${eyesClosed.value} ${eyesOpened.value}")
            if(isRightEyeClosed && isLeftEyeClosed){
                //increase step by 1
                eyesClosed.value =  (eyesClosed.value?:0)+1
                delay(200)
                shouldWait=false
                return
            }
            else{
                eyesOpened.value =  (eyesOpened.value?:0)+1
                delay(200)
                shouldWait=false
                return
            }


    }



    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }


    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(viewLifecycleOwner) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                        Toast.makeText(
                            context,
                            "CameraState: Pending Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        Toast.makeText(
                            context,
                            "CameraState: Opening",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                        Toast.makeText(
                            context,
                            "CameraState: Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                        Toast.makeText(
                            context,
                            "CameraState: Closing",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                        Toast.makeText(
                            context,
                            "CameraState: Closed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        Toast.makeText(
                            context,
                            "Stream config error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        Toast.makeText(
                            context,
                            "Camera in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        Toast.makeText(
                            context,
                            "Max cameras in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                        Toast.makeText(
                            context,
                            "Other recoverable error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        Toast.makeText(
                            context,
                            "Camera disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        Toast.makeText(
                            context,
                            "Fatal error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Toast.makeText(
                            context,
                            "Do not disturb mode enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


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
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Timber.e(exc, getString(R.string.photo_capture_failed) + exc.message)
                        showErrorSnackbar(R.string.photo_capture_failed,binding.root)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri: Uri = output.savedUri ?: Uri.fromFile(photoFile)

                        try {

                        photoUri = savedUri.toString()

                        lifecycleScope.launch (Dispatchers.Main) {
                            binding.imagePreviewView.loadGlide(photoFile)
                            try {
                                binding.apply {
                                    selectButton.show()
                                    retakeButton.show()
                                    cameraCaptureButton.hide()
                                    blinkCountText.hide()
                                    this.overlay.hide()
                                    this.circleText.hide()
                                    this.flipButton.hide()
                                    this.graphicOverlayFinder.hide()
                                    viewFinder.hide()
                                    imagePreviewView.show()
                                    retakeButton.setOnClickListener {
                                        photoFile.delete()
                                        viewModel.profileImage = null
                                        showCamera()
                                    }
                                    selectButton.setOnClickListener {
                                        viewModel.profileImage = photoFile.path
                                        findNavController().navigateUp()
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e)
                                FirebaseCrashlytics.getInstance().recordException(e)
                            }
                        }
                        }
                        catch (e: IOException) {
                            e.printStackTrace()
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                    }
                })

            // We can only change the foreground Drawable using API level 23+ API

            // Display flash animation to indicate that photo was captured

                binding.root.postDelayed({
                    binding.root.foreground = ColorDrawable(Color.WHITE)
                    binding.root.postDelayed(
                        { binding.root.foreground = null }, ANIMATION_FAST_MILLIS
                    )
                }, ANIMATION_SLOW_MILLIS)

        }
    }


private fun showCamera() = with(binding) {
    cameraCaptureButton.show()
    viewFinder.show()
    blinkCountText.show()
    imagePreviewView.hide()
    selectButton.hide()
    retakeButton.hide()
    this.overlay.show()
    this.circleText.show()
    this.flipButton.show()
    this.graphicOverlayFinder.show()
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
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )

        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }
}

 fun ImageView.loadGlide(photoFile: File) {
    Glide.with(this).load(photoFile).into(this)
}


/** Milliseconds used for UI animations */
const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 100L




enum class CaptureProgressState(){
     CLOSE_EYE, BLINK,TAKE_PHOTO
}
