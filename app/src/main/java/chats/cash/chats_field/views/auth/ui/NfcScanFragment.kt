package chats.cash.chats_field.views.auth.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentNfcScanBinding
import chats.cash.chats_field.utils.ChatsFieldConstants
import chats.cash.chats_field.utils.ChatsFieldConstants.NFC_BUNDLE_KEY
import chats.cash.chats_field.utils.dialogs.AlertDialog
import chats.cash.chats_field.utils.dialogs.getErrorDialog
import chats.cash.chats_field.utils.dialogs.getSuccessDialog
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.nfc.NfcManager
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.dialog.DeviceSelectorDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@InternalCoroutinesApi
class NfcScanFragment : BottomSheetDialogFragment() {

    private var isOffline: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    private fun openDeviceSelector() {
        val bottomSheetDialogFragment = DeviceSelectorDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = true
        bottomSheetDialogFragment.setTargetFragment(this, ChatsFieldConstants.CONNECTION_CODE)
        bottomSheetDialogFragment.show(
            requireFragmentManager().beginTransaction(),
            "BottomSheetDialog",
        )
    }

    lateinit var binding: FragmentNfcScanBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNfcScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var NfcManager: NfcManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isOffline = requireArguments().getBoolean("isOffline")
        if (isOffline) {
            binding.offlineText.show()
        } else {
            binding.offlineText.hide()
        }
        // setUpData()
        NfcManager = NfcManager(requireActivity())
        if (NfcManager.isNFCSupported()) {
            if (NfcManager.isNfcEnabled()) {
            } else {
                lifecycleScope.launch {
                    delay(1000)
                    AlertDialog(
                        requireContext(),
                        getString(R.string.enable_nfc),
                        getString(R.string.nfc_disabled_message),
                        postiveText = getString(R.string.enable),
                        onNegativeClicked = {
                            this@NfcScanFragment.dismiss()
                        },
                    ) {
                        NfcManager.goToEnableNfcSettings()
                    }.show()
                }
            }
        } else {
            binding.scanNfcBtn.isEnabled = false
            val error = getErrorDialog(getString(R.string.nfc_not_supported), requireContext())
            lifecycleScope.launch {
                delay(1000)
                error.show()
            }
        }

        registerClickEvents()
    }

    private fun registerClickEvents() {
        binding.scanNfcBtn.setOnClickListener {
            if (!NfcManager.scanMode) {
                NfcManager.scanMode = true
                binding.scanNfcBtn.text = getString(R.string.nfc_place_nfc_tag_close_to_)
            } else {
                NfcManager.scanMode = false
                binding.scanNfcBtn.text = getString(R.string.click_here_to_start_scanning)
            }
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        registerNFCCallbacks()
    }

    private fun registerNFCCallbacks() {
        val data =
            requireArguments().getString(emailBundle)?.trim()?.replace(" ", "")?.replace("\n", "")
        NfcManager.tagData = data
        binding.offlineText.hide()

        NfcManager.onSuccess = {
            lifecycleScope.launch(Dispatchers.Main) {
                dismiss()
                val dialog =
                    getSuccessDialog(getString(R.string.tag_written_successfully), requireContext())
                dialog.show()
            }
        }
        NfcManager.onSuccessFullRead = {
            lifecycleScope.launch(Dispatchers.Main) {
                dismiss()
                val dialog =
                    getSuccessDialog("Data read from ${it.replace("?", "")}", requireContext())
                dialog.show()
            }
        }

        NfcManager.onError = {
            lifecycleScope.launch(Dispatchers.Main) {
                dismiss()
                val error =
                    getErrorDialog(
                        getString(R.string.an_error_occurred_while_writing_to_card_try_again),
                        requireContext(),
                    )
                error.show()
            }
        }

        NfcManager.onAuthError = {
            lifecycleScope.launch(Dispatchers.Main) {
                dismiss()
                val error =
                    getErrorDialog(
                        getString(R.string.the_card_couldnt_be_authenticated),
                        requireContext(),
                    )
                error.show()
            }
        }

        NfcManager.onRead = {
            lifecycleScope.launch(Dispatchers.Main) {
                val dialog =
                    getSuccessDialog(
                        getString(R.string.tag_read_successfully, it),
                        requireContext(),
                    )
                dialog.show()
            }
        }

        NfcManager.onErrorReading = {
            lifecycleScope.launch(Dispatchers.Main) {
                dismiss()
                val error =
                    getErrorDialog(
                        getString(R.string.error_reading_card_try_again, it),
                        requireContext(),
                    )
                error.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        NfcManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        NfcManager.onResume()
        Timber.d(requireActivity().intent.action)
    }

    companion object {
        // Creates a new Instance of this dialog
        fun newInstance(isOffline: Boolean, data: String): NfcScanFragment =
            NfcScanFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isOffline", isOffline)
                    putString(emailBundle, data)
                }
            }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        setFragmentResult(NFC_REQUEST_KEY, bundleOf(NFC_BUNDLE_KEY to true))
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

const val emailBundle = "data"
