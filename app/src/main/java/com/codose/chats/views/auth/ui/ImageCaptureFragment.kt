package com.codose.chats.views.auth.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.codose.chats.R
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.utils.toast
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.codose.chats.views.base.BaseFragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.fragment_image_capture.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class ImageCaptureFragment : BaseFragment() {
    private var isCaptured : Boolean = false
    private var blinkCount = 0
    private lateinit var verifyTask: Runnable
    private var eyesOpen = false
    private var eyesOpenCount = 0
    inner class ImageProcessor : ImageAnalysis.Analyzer {

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                Timber.v("New Frame : ${image.format}")
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        if(faces.isEmpty()){
                            blinkCount = 0
                            //setBlinkCount()
                            //When there's no face, reset the blink counts
                        }
                        // Returns an array containing all the detected faces
                        faces.forEach { face ->
                            Timber.v("LeftEyeOpen Probability : ${face.leftEyeOpenProbability}")
                            Timber.v("RightEyeOpen Probability : ${face.rightEyeOpenProbability}")
                            if(face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null){
                                // Used to check if eyes is shut
                                eyesShut = if (!firstCondtionPassed){
                                    (face.leftEyeOpenProbability<0.5) && (face.rightEyeOpenProbability <0.5)
                                } else{
                                    false
                                }
                                // Used to check if has been open after eyes was shut for 5 seconds
                                if (firstCondtionPassed && (face.leftEyeOpenProbability>0.5) && (face.rightEyeOpenProbability >0.5)){
                                    eyesOpenCount += 1
                                    eyesOpen = true
                                }
                                //Used to check if the user can blink after eyes has been shot for 5 seconds
                                canBlink = (firstCondtionPassed && (eyesOpenCount>5))
                                if (canBlink){
                                    setBlinkCount()
                                }
                                Timber.v("Eyes shut: $eyesShut ")
                                /*if ((face.leftEyeOpenProbability < 0.5) && (face.rightEyeOpenProbability >= 0.3)) {
                                    //Since Average Blink Count for a Human is about 20 per Minute, This Will try to detect at least 5 blink counts
                                        hasBlinkedLeftEye = true
                                    //setBlinkCount(blinkCount)
                                    Timber.v("Blinking: $hasBlinkedLeftEye")
                                }
                                if (face.rightEyeOpenProbability < 0.5 && hasBlinkedLeftEye && face.leftEyeOpenProbability >= 0.3){
                                    hasBlinkedRightEye = true
                                }*/
                            }

                        }
                    }
                    .addOnFailureListener {
                        Timber.e(it)
                    }
                    .addOnCompleteListener {
                        mediaImage.close()
                        imageProxy.close()
                        Timber.v("Image Closed")
                    }
            }
        }
    }

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var highAccuracyOpts: FaceDetectorOptions
    private lateinit var detector: FaceDetector

    private var firstCondtionPassed = false
    private var canBlink = false
    private val viewModel by sharedViewModel<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // High-accuracy landmark detection and face classification
        highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        // Request camera permission
        detector = FaceDetection.getClient(highAccuracyOpts)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        camera_capture_button.setOnClickListener { takePhoto() }
        verifyTask()

        // Set up the listener for take photo button

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private var firstTimeShowing = true
    private var eyesShutSecCount = 0
    private var eyesShut = false
    private fun verifyTask(){
        var handler = Handler()

        verifyTask = Runnable {
            if (eyesShut){
                // Increase the duration of time eyes shut by 1 second
                eyesShutSecCount +=1
                Timber.v("Eyes shut count $eyesShutSecCount")
                if (eyesShutSecCount >=5){
                    progressbar.visibility = GONE
                    blinkCountText.text = "Now blink your eyes"
                    // set initial condition of eyes being shut as true, User has passed first condition
                    firstCondtionPassed = true
                    //Reset duration of eyes shut back to zero
                    if (firstCondtionPassed) eyesShutSecCount = 0
                }
                //handler.postDelayed(verifyTask, 1000)
            }

            // If the user Opens his eyes before 5 seconds reset the eyes shut time back to zero
            else eyesShutSecCount = 0
            try {
                progressbar.progress = eyesShutSecCount
            }
            catch (e: Exception){

            }

            if (firstCondtionPassed && canBlink && eyesOpen){
                // call function for activating the capture button
                // Stop thread
                handler.removeCallbacksAndMessages(null)
            }
            if (!firstCondtionPassed && eyesShut)
                handler.postDelayed(verifyTask, 1000)
            else
                handler.postDelayed(verifyTask, 10)
        }
        verifyTask.run()
    }


    private fun takePhoto() {
        isCaptured = true
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        camera_capture_button.isEnabled = false
        showToast("Processing image please wait...")
        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val image: InputImage
                    try {
                        image = InputImage.fromFilePath(context, savedUri)

                        detector.process(image)
                            .addOnSuccessListener { faces ->
                                if (faces.isEmpty() && blinkCount < 3) {
                                    photoFile.delete()
                                    camera_capture_button.isEnabled = true
                                    blinkCount = 0
                                    //setBlinkCount(blinkCount)
                                    isCaptured = false
                                    requireContext().toast("No Real Face detected, Please retake photo.")
                                } else {
                                    hideCamera(photoFile)
                                }

                            }
                            .addOnFailureListener { e ->

                            }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            })
    }

    private fun setBlinkCount() {
        try {
            blinkCountText.text = "Face Detected"
            camera_capture_button.isEnabled = true
        }
        catch (e: java.lang.Exception){

        }

    }

    private fun hideCamera(photoFile: File) {
        Glide.with(requireContext())
            .load(photoFile)
            .into(imagePreviewView)
        try {
            select_button.show()
            retake_button.show()
            camera_capture_button.hide()
            viewFinder.hide()
            imagePreviewView.show()
            retake_button.setOnClickListener {
                photoFile.delete()
                showCamera()
            }
            select_button.setOnClickListener {
                viewModel.profileImage = photoFile.path
                findNavController().navigateUp()
            }
            isCaptured = false
        }catch (e : Exception){

        }

//        val bitmap = BitmapFactory.decodeFile(photoFile.path)
//        Glide.with(requireContext())
//            .asBitmap()
//            .load(photoFile)
//            .into(imagePreviewView)


    }

    private fun showCamera() {
        camera_capture_button.show()
        viewFinder.show()
        imagePreviewView.hide()
        select_button.hide()
        retake_button.hide()
        blinkCount = 0
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        requireContext().toast("Initializing Camera....Please Wait")

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, ImageProcessor())

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requireContext().toast("Permissions not granted by the user.")
                findNavController().navigateUp()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}

