package chats.cash.chats_field.views.auth.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentNfcScanBinding
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.NFC_BUNDLE_KEY
import chats.cash.chats_field.utils.dialogs.AlertDialog
import chats.cash.chats_field.utils.dialogs.getErrorDialog
import chats.cash.chats_field.utils.dialogs.getSuccessDialog
import chats.cash.chats_field.utils.nfc.NfcManager
import chats.cash.chats_field.views.auth.dialog.DeviceSelectorDialog
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*

@InternalCoroutinesApi
class NfcScanFragment : BottomSheetDialogFragment() {

    private val mViewModel by sharedViewModel<RegisterViewModel>()

    private var mDeviceCmd: Byte = 0x00
    private var mIsWork = false
    private var mCmdData = ByteArray(10240)
    private var mCmdSize = 0

    private var mTimerTimeout: Timer? = null
    private var mTaskTimeout: TimerTask? = null
    private var mHandlerTimeout: Handler? = null

    // Name of the connected device
    private var mConnectedDeviceName: String? = null

    // String buffer for outgoing messages
    private var mOutStringBuffer: StringBuffer? = null

    // Local Bluetooth adapter
    private var mBluetoothAdapter: BluetoothAdapter? = null

    // Member object for the chat services
    private var mChatService: BluetoothReaderService? = null


    var mCardSn = ByteArray(7) // image data

    var mUpImageSize = 0

    private var isOffline: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)


    }

    private fun openDeviceSelector() {
        val bottomSheetDialogFragment = DeviceSelectorDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = true
        bottomSheetDialogFragment.setTargetFragment(this, ChatsFieldConstants.CONNECTION_CODE)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),
            "BottomSheetDialog")
    }

    lateinit var binding:FragmentNfcScanBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNfcScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var NfcManager:NfcManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isOffline = requireArguments().getBoolean("isOffline")
        if (isOffline) {
            binding.offlineText.show()
        } else {
            binding.offlineText.hide()
        }
        //setUpData()
        NfcManager = NfcManager(requireActivity())
        if(NfcManager.isNFCSupported()){
            if(NfcManager.isNfcEnabled()){

            }
            else{
                lifecycleScope.launch {
                    delay(1000)
                    AlertDialog(requireContext(),
                        getString(R.string.enable_nfc),
                        getString(R.string.nfc_disabled_message),
                        postiveText = getString(R.string.enable),
                        onNegativeClicked = {
                            this@NfcScanFragment.dismiss()
                        }) {
                        NfcManager.goToEnableNfcSettings()
                    }.show()
                }
            }
        }
        else{
            binding.scanNfcBtn.isEnabled = false
            val error = getErrorDialog(getString(R.string.nfc_not_supported), requireContext())
            lifecycleScope.launch {
                delay(1000)
                error.show()
            }

        }

        registerClickEvents()
    }
    private var isInScanMode = false
    private fun registerClickEvents(){
        binding.scanNfcBtn.setOnClickListener {
            if(!NfcManager.scanMode){
                NfcManager.scanMode=true
                binding.scanNfcBtn.text = getString(R.string.nfc_place_nfc_tag_close_to_)
            }
            else{
                NfcManager.scanMode=false
                binding.scanNfcBtn.text = getString(R.string.click_here_to_start_scanning)
            }
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        registerNFCCallbacks()
    }


    private fun registerNFCCallbacks(){
        val email = requireArguments().getString(emailBundle)
        NfcManager.userEmail = email
        NfcManager.onSuccess = {
            lifecycleScope.launch(Dispatchers.Main) {
                dismiss()
                val dialog =
                    getSuccessDialog("Tag written successfully", requireContext())
                dialog.show()
            }
        }
        NfcManager.onSuccessFullRead = {
            lifecycleScope.launch(Dispatchers.Main) {
              //  dismiss()
                val dialog =
                    getSuccessDialog("Data read from ${it.replace("?","")}", requireContext())
                dialog.show()
            }
        }

        NfcManager.onError = {
            lifecycleScope.launch(Dispatchers.Main) {
               // dismiss()
                val error =
                    getErrorDialog("An error occurred while writing to card, try again", requireContext())
                error.show()
            }
        }
        NfcManager.onRead = {
            lifecycleScope.launch(Dispatchers.Main) {
                val dialog =
                    getSuccessDialog("Tag Read successfully $it", requireContext())
                dialog.show()
            }
        }

        NfcManager.onErrorReading = {
            lifecycleScope.launch(Dispatchers.Main) {
                //dismiss()
                val error =
                    getErrorDialog("error reading card $it, try again", requireContext())
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
        //Creates a new Instance of this dialog
        fun newInstance(isOffline: Boolean, email:String): NfcScanFragment =
            NfcScanFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isOffline", isOffline)
                    putString(emailBundle,email)
                }
            }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        setFragmentResult(NFC_REQUEST_KEY, bundleOf(NFC_BUNDLE_KEY to true))

    }

    override fun onDestroy() {
        super.onDestroy()
        if (mChatService != null) {
            mChatService!!.stop()
        }
    }
}

const val emailBundle = "Email"
