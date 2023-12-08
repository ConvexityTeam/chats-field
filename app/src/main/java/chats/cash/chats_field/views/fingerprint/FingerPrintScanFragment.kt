package chats.cash.chats_field.views.fingerprint

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentFingerprintScanningBinding
import chats.cash.chats_field.utils.fingerprint.FingerPrintManager
import chats.cash.chats_field.utils.fingerprint.FingerPrintScanCallback
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.utils.showToast
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FingerPrintScanFragment :
    Fragment(R.layout.fragment_fingerprint_scanning), FingerPrintScanCallback {

    private var _binding: FragmentFingerprintScanningBinding? = null
    private val binding get() = _binding!!

    val fingerPrintManager by lazy {
        FingerPrintManager(requireContext(), this)
    }

    lateinit var viewModel: RegisterViewModel
    var progress = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFingerprintScanningBinding.bind(view)
        fingerPrintManager.initSdk()
        binding.loading.show()
        binding.prints.setImageBitmap(null)
        init()
        val viewmodel by activityViewModel<RegisterViewModel>()
        viewModel = viewmodel
        progress = viewModel.fingerPrintScanProgress
        init(viewModel.fingerPrintScanProgress)
    }

    private fun init() {
        binding.fingerprintAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        fingerPrintManager.startEnrolling()
        binding.save.hide()
    }

    override fun onDeviceMessage(message: String) {
        binding.scanHint.text = message
        if (message.equals(getString(R.string.open_device_ok), true)) {
            fingerPrintManager.startEnrolling()
        }
    }

    var bitmap: Bitmap? = null
    override fun onNewImage(image: Bitmap) {
        binding.prints.setImageBitmap(image)
        binding.loading.hide()
        bitmap = image
    }

    override fun onTimeOut() {
        showToast(getString(R.string.timeout))
        binding.scanHint.text = getString(R.string.timeout)
    }

    override fun onEnroll() {
        //binding.scanButton.disable()
    }

    override fun onEnrollSuccess() {
        binding.save.show()
        binding.save.setOnClickListener {
            setFragmentResult(FINGER_SCAN_REQUEST_KEY, bundleOf(FINGER_SCANNED_BITMAP_KEY to bitmap))
            fingerPrintManager.onStop()
            findNavController().navigateUp()
        }
        bitmap?.let { viewModel.savFingerPrint(it, progress, {}) {} }
    }

    override fun onResume() {
        super.onResume()
        fingerPrintManager.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        fingerPrintManager.onStop()
    }

    var currentFingerToScan: FingersToScan = FingersToScan.LeftThumb

    private fun init(progress: Int) {
        binding.fingerprintAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        when (progress) {
            0 -> {
                currentFingerToScan = FingersToScan.LeftThumb
            }

            1 -> {
                currentFingerToScan = FingersToScan.leftIndex
            }

            2 -> {
                currentFingerToScan = FingersToScan.leftLittle
            }

            3 -> {
                currentFingerToScan = FingersToScan.rightThumb
            }

            4 -> {
                currentFingerToScan = FingersToScan.rightIndex
            }

            5 -> {
                currentFingerToScan = FingersToScan.rightLittle
            }

            6 -> {
                currentFingerToScan = FingersToScan.done
            }
        }
        updateUiWithFInger(currentFingerToScan)
    }

    private fun updateUiWithFInger(item: FingersToScan) {
        if (item != FingersToScan.done) {
            getString(item.name).apply {
                binding.title.text = getString(R.string.scan_template_finger, this)
                binding.desc.text = getString(R.string.fingerprint_place_template, this)
            }
        } else {
            binding.title.text = getString(R.string.verify_fingerprint)
            binding.desc.text = getString(R.string.allfingers_scanned)
        }

        when (item) {
            FingersToScan.LeftThumb -> {
                binding.rightImage.setImageResource(R.drawable.right_icon_fingerprint_none)
                binding.leftImage.setImageResource(R.drawable.left_thumb_scan)
            }
            FingersToScan.leftIndex -> {
                binding.rightImage.setImageResource(R.drawable.right_icon_fingerprint_none)
                binding.leftImage.setImageResource(R.drawable.left_index_scan)
            }
            FingersToScan.leftLittle -> {
                binding.rightImage.setImageResource(R.drawable.right_icon_fingerprint_none)
                binding.leftImage.setImageResource(R.drawable.left_little_finger)
            }
            FingersToScan.rightIndex -> {
                binding.rightImage.setImageResource(R.drawable.left_index_scan)
                binding.leftImage.setImageResource(R.drawable.finger_scan_all_done)
            }
            FingersToScan.rightLittle -> {
                binding.rightImage.setImageResource(R.drawable.left_little_finger)
                binding.leftImage.setImageResource(R.drawable.finger_scan_all_done)
            }
            FingersToScan.rightThumb -> {
                binding.rightImage.setImageResource(R.drawable.left_thumb_scan)
                binding.leftImage.setImageResource(R.drawable.finger_scan_all_done)
            }

            FingersToScan.done -> {
                binding.rightImage.setImageResource(R.drawable.finger_scan_all_done)
                binding.leftImage.setImageResource(R.drawable.finger_scan_all_done)
            }
        }
    }
}
