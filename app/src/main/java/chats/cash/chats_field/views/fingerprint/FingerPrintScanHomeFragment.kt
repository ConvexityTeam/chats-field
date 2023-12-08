package chats.cash.chats_field.views.fingerprint

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentFingerprintScanningHomeBinding
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import chats.cash.chats_field.views.core.dialogs.showAlertDialog
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

class FingerPrintScanHomeFragment :
    BaseFragment() {

    private var _binding: FragmentFingerprintScanningHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFingerprintScanningHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewmodel by activityViewModel<RegisterViewModel>()
        viewModel = viewmodel
        init(viewModel.fingerPrintScanProgress)

        setFragmentResultListener(FINGER_SCAN_REQUEST_KEY) { res, bundle ->

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(FINGER_SCANNED_BITMAP_KEY, Bitmap::class.java)
            } else {
                bundle.getParcelable(FINGER_SCANNED_BITMAP_KEY) as Bitmap?
            }

            init(viewModel.fingerPrintScanProgress)
            Timber.tag("RESULT").v("bitmap $bitmap")
        }
    }

    var currentFingerToScan: FingersToScan = FingersToScan.LeftThumb

    private fun init(progress: Int) {
        Timber.tag("SIZE").v("size is ${viewModel.fingerPrintsScanned.value.size}")

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

        binding.restartScan.setOnClickListener {
            requireContext().showAlertDialog(
                R.string.restart_scan,
                R.string.restart_scan_desc,
                positiveText = R.string.restart,
            ) {
                viewModel.restartFingerPrintScan()
                init(viewModel.fingerPrintScanProgress)
            }.show()
        }
    }

    private fun updateUiWithFInger(item: FingersToScan) {
        if (item != FingersToScan.done) {
            getString(item.name).apply {
                binding.title.text = getString(R.string.scan_template_finger, this)
                binding.desc.text = getString(R.string.fingerprint_place_template, this)
                binding.startScan.text = getString(R.string.scan_template_finger, this)
                binding.startScan.setOnClickListener {
                    findNavController().navigate(R.id.action_fingerPrintScanHomeFragment_to_fingerPrintScanFragment)
                }
            }
        } else {
            if ((viewModel.fingerPrintsScanned.value?.size ?: 0) >= 6) {
                binding.title.text = getString(R.string.verify_fingerprint)
                binding.desc.text = getString(R.string.allfingers_scanned)
                binding.startScan.text = getString(R.string.complete_onboarding)
                binding.startScan.setOnClickListener {
                    viewModel.specialCase = false
                    findNavController().navigate(R.id.action_fingerPrintScanHomeFragment_to_submitBeneficiary)
                }
            } else {
                requireContext().showAlertDialog(
                    R.string.restart_scan,
                    R.string.something_went_wrong_while_scanning,
                    positiveText = R.string.restart,
                    onDismiss = {
                        findNavController().navigateUp()
                    },
                ) {
                    viewModel.restartFingerPrintScan()
                    init(viewModel.fingerPrintScanProgress)
                }.show()
            }
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

sealed class FingersToScan(val name: Int) {
    object LeftThumb : FingersToScan(R.string.left_thumb)
    object rightThumb : FingersToScan(R.string.right_thumb)
    object leftIndex : FingersToScan(R.string.leftIndex)
    object rightIndex : FingersToScan(R.string.rightIndex)
    object rightLittle : FingersToScan(R.string.rightLittle)
    object leftLittle : FingersToScan(R.string.leftLittle)
    object done : FingersToScan(R.string.done)
}

const val FINGER_SCAN_REQUEST_KEY = "19234"
const val FINGER_SCANNED_BITMAP_KEY = "SCANNED_BITMAP"
