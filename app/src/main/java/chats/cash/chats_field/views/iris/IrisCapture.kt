package chats.cash.chats_field.views.iris

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentIrisCaptureScreenBinding
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.iris_scan.IrisCallBack
import chats.cash.chats_field.utils.iris_scan.IrisManager
import chats.cash.chats_field.utils.show
import com.eyecool.fragment.IrisFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [IrisCapture.newInstance] factory method to
 * create an instance of this fragment.
 */
class IrisCapture : AppCompatActivity(), IrisCallBack {

    private var _binding: FragmentIrisCaptureScreenBinding? = null
    private val binding: FragmentIrisCaptureScreenBinding
        get() = _binding!!

    lateinit var irisSdk: IrisManager
    private var irisFragment: IrisFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FragmentIrisCaptureScreenBinding.inflate(layoutInflater)
        setContentView(_binding?.root)
        irisFragment = supportFragmentManager.findFragmentById(R.id.irisFragment) as IrisFragment?
        irisFragment = fragmentManager.findFragmentById(R.id.irisFragment) as IrisFragment
        onViewCreated()
    }

    fun init() {
        binding.backBtn.setOnClickListener {
            finish()
            setResult(RESULT_CANCELED)
        }
        irisSdk.initIrisSDK()
        irisFragment?.openCamera()
    }

    fun onViewCreated() {
        irisSdk =
            IrisManager(this, irisFragment, this)
        init()
        binding.tryagainButton.setOnClickListener {
            binding.errorGroup.hide()
            init()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Device is in landscape mode
            // Perform necessary actions for landscape mode
            binding.apply {
                errorGroup.hide()
                successsGroup.show()
                loadingGroup.hide()
                rotateGroup.hide()
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Device is in portrait mode
            // Perform necessary actions for portrait mode
            binding.apply {
                errorGroup.hide()
                successsGroup.hide()
                loadingGroup.hide()
                rotateGroup.show()
            }
        }
    }

    override fun OnIrisSdkInitialized(version: String) {
        irisFragment = supportFragmentManager.findFragmentById(R.id.irisFragment) as IrisFragment?
        binding.loadingLayout.hide()
        binding.errorLayout.hide()
        binding.readyGroup.show()
        binding.enrollButton?.setOnClickListener {
            lifecycleScope.launch {
                irisSdk.enroll().await()
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onPause() {
        super.onPause()
        irisFragment?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun OnIrisInitializationError(msg: String) {
        binding.readyGroup.hide()
        binding.loadingLayout.hide()
        binding.successLayout.hide()
        binding.errorLayout.show()
        binding.errorDesc.text = getString(R.string.iris_sdk_wasn_t_initialized, msg)
        binding.enrollButton.isEnabled = false
    }

    override fun OnEnrollProgressChange(leftEyeProgress: Int, rightEyeProgress: Int) {
        (binding.leftEyeProgress as CircularProgressIndicator).isIndeterminate = false
        (binding.leftEyeProgress as CircularProgressIndicator).progress = leftEyeProgress
        (binding.rigtEyeProgress as CircularProgressIndicator).isIndeterminate = false
        (binding.rigtEyeProgress as CircularProgressIndicator).progress = rightEyeProgress
        binding.enrollButton.isEnabled = false
    }

    fun resetProgress() {
        (binding.leftEyeProgress as CircularProgressIndicator).isIndeterminate = true
        (binding.rigtEyeProgress as CircularProgressIndicator).isIndeterminate = true
    }

    override fun OnEnrollError(reason: String) {
        binding.readyGroup.hide()
        binding.loadingLayout.hide()
        binding.successLayout.hide()
        binding.errorLayout.show()
        binding.errorDesc.text = getString(R.string.iris_enrollment_error, reason)
        resetProgress()
        binding.enrollButton?.isEnabled = true
    }

    override fun OnEnrollSuccess(byte: String) {
        binding.readyGroup.hide()
        binding.loadingLayout.hide()
        binding.successLayout.show()
        binding.successDesc.text = getString(R.string.enrollment_of_user_was_successful_please_wait)
        binding.errorLayout.hide()
        binding.enrollButton?.isEnabled = true

        lifecycleScope.launch {
            delay(2000)
            val returnIntent = Intent()
            returnIntent.putExtra(IRIS_ENROLL_PARAM, byte)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    override fun OnEnrollSuccess(left: String, right: String) {
    }

    override fun OnMakeAdjustMent(adjustment: String) {
        binding.desc.text = adjustment
    }

    override fun AdjustmentOkay() {
        binding.desc.text = getString(R.string.enrolling_iris_please_keep_still)
    }

    override fun verifyingUser() {
        TODO("Not yet implemented")
    }

    override fun VerificationError(message: String) {
        TODO("Not yet implemented")
    }

    override fun UserVerified(name: String) {
        TODO("Not yet implemented")
    }

    override fun CameraOpenError(error: Int) {
        //   binding.readyGroup.hide()
        // binding.loadingLayout.hide()
        //binding.successLayout.hide()
        //binding.errorLayout.show()
        //binding.errorDesc.text = "Camera can not be opened: ${error}"
    }
}

const val IRIS_ENROLL_PARAM = "iris"
