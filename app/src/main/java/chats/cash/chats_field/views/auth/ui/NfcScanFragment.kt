package chats.cash.chats_field.views.auth.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentNfcScanBinding
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.EXTRA_DEVICE_ADDRESS
import chats.cash.chats_field.utils.ChatsFieldConstants.FRAGMENT_NFC_RESULT_LISTENER
import chats.cash.chats_field.utils.ChatsFieldConstants.NFC_BUNDLE_KEY
import chats.cash.chats_field.utils.dialogs.AlertDialog
import chats.cash.chats_field.utils.dialogs.getErrorDialog
import chats.cash.chats_field.utils.dialogs.getSuccessDialog
import chats.cash.chats_field.utils.nfc.NdefMessageParser
import chats.cash.chats_field.utils.nfc.NfcManager
import chats.cash.chats_field.views.auth.dialog.DeviceSelectorDialog
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_nfc_scan.*
import kotlinx.coroutines.*
import okhttp3.internal.and
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
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                ChatsFieldConstants.PERMISSIONS_STORAGE,
                ChatsFieldConstants.REQUEST_PERMISSION_CODE)
        }
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
            offlineText.show()
        } else {
            offlineText.hide()
        }
        setUpData()
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
                binding.scanNfcBtn.text = "Click here to start scanning"
            }
        }
        registerNFCCallbacks()
    }


    private fun registerNFCCallbacks(){
        val email = requireArguments().getString(emailBundle)
        NfcManager.ndefMessage = email
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
                dismiss()
                val dialog =
                    getSuccessDialog("Data read from ${it.replace("?","")}", requireContext())
                dialog.show()
            }
        }

        NfcManager.onError = {
            lifecycleScope.launch(Dispatchers.Main) {
                dismiss()
                val error =
                    getErrorDialog("An error occured while writting to card", requireContext())
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
                dismiss()
                val error =
                    getErrorDialog("error reading card $it", requireContext())
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




    private fun setUpData() {
        val bluetoothPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            }
        val bluetoothMultiplePermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            }
        // The Handler that gets information back from the BluetoothChatService
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, ChatsFieldConstants.REQUEST_ENABLE_BT)
                // Otherwise, setup the chat session
            } else {
                if (mChatService == null) setupChat()
            }
            scanNfcBtn.text = "Connect Device"
            scanNfcBtn.setOnClickListener {
                openDeviceSelector()
            }
            closeButton.setOnClickListener { dismiss() }
        } else {
            // request for bluetooth permission
            showToast("Bluetooth permission not granted")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothPermissionLauncher.launch(enableBtIntent)
            }
        }
    }

    private fun setupChat() {
        @SuppressLint("HandlerLeak") val mHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    ChatsFieldConstants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                        BluetoothReaderService.STATE_CONNECTED -> {

                        }
                        BluetoothReaderService.STATE_CONNECTING -> {

                        }
                        BluetoothReaderService.STATE_LISTEN, BluetoothReaderService.STATE_NONE -> {

                        }
                    }
                    ChatsFieldConstants.MESSAGE_WRITE -> {

                    }
                    ChatsFieldConstants.MESSAGE_READ -> {
                        val readBuf = msg.obj as ByteArray
                        if (readBuf.isNotEmpty()) {
                            if (readBuf[0] == 0x1b.toByte()) {
                                //
                            } else {
                                ReceiveCommand(readBuf, msg.arg1)
                            }
                        }
                    }
                    ChatsFieldConstants.MESSAGE_DEVICE_NAME -> {
                        // save the connected device's name
                        mConnectedDeviceName = msg.data.getString(ChatsFieldConstants.DEVICE_NAME)
                        Toast.makeText(requireContext(),
                            "Connected to $mConnectedDeviceName",
                            Toast.LENGTH_SHORT).show()
                        scanNfcBtn.text = "Scan NFC Card"
                        scanNfcBtn.setOnClickListener {
                            SendCommand(ChatsFieldConstants.CMD_CARDSN, null, 0)
                        }

                    }
                    ChatsFieldConstants.MESSAGE_TOAST -> {
                        try {
                            Toast.makeText(requireContext(),
                                msg.data.getString(
                                    ChatsFieldConstants.TOAST),
                                Toast.LENGTH_SHORT).show()
                        } catch (e: Throwable) {
                            Timber.v(e)
                        }

                    }
                }
            }
        }
        mChatService = BluetoothReaderService(requireContext(),
            mHandler) // Initialize the BluetoothChatService to perform bluetooth connections

        mOutStringBuffer = StringBuffer("")

    }

    private fun ReceiveCommand(databuf: ByteArray, datasize: Int) {
        //other data received from the device
        // append the databuf received into mCmdData.
        FingerPrintUtils.memcpy(mCmdData, mCmdSize, databuf, 0, datasize)
        mCmdSize = mCmdSize + datasize
        val totalsize: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00) + 9
        if (mCmdSize >= totalsize) {
            mCmdSize = 0
            mIsWork = false
            timeOutStop()
            //parsing the mCmdData
            if (mCmdData[0] == 'F'.toByte() && mCmdData[1] == 'T'.toByte()) {
                val size: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xF0) - 1
                if (size > 0) {
                    FingerPrintUtils.memcpy(mCardSn, 0, mCmdData, 8, size)
                    val cardUID =
                        Integer.toHexString(mCardSn[0] and 0xFF) + Integer.toHexString(
                            mCardSn[1] and 0xFF) + Integer.toHexString(mCardSn[2] and 0xFF) + Integer.toHexString(
                            mCardSn[3] and 0xFF) + Integer.toHexString(mCardSn[4] and 0xFF) + Integer.toHexString(
                            mCardSn[5] and 0xFF) + Integer.toHexString(mCardSn[6] and 0xFF)
                    requireContext().toast("NFC Card scan successfully")
                    mViewModel.nfc = cardUID
                    targetFragment!!.onActivityResult(targetRequestCode,
                        Activity.RESULT_OK,
                        Intent().putExtra("isOffline", isOffline))
                    dismiss()
                    Timber.v("Read Card SN Succeed:" + cardUID)
                } else {
                    Timber.v("Search Fail")
                    requireContext().toast("Unable to scan card")
                }
//                    when (mCmdData[4]) {
//                        CMD_CARDSN -> {
//
//                        }
//                        else -> {
//                            printProgress.hide()
//                            Timber.v("Search Fail_${mCmdData[4]}")
//                        }
//                    }
            }
        }
    }


    fun SendCommand(cmdid: Byte, data: ByteArray?, size: Int) {
        if (mIsWork) return
        val sendsize = 9 + size
        val sendbuf = ByteArray(sendsize)
        sendbuf[0] = 'F'.toByte()
        sendbuf[1] = 'T'.toByte()
        sendbuf[2] = 0
        sendbuf[3] = 0
        sendbuf[4] = cmdid
        sendbuf[5] = size.toByte()
        sendbuf[6] = (size shr 8).toByte()
        if (size > 0 && data != null) {
            for (i in 0 until size) {
                sendbuf[7 + i] = data[i]
            }
        }
        val sum: Int = ChatsFieldCommands.calcCheckSum(sendbuf, 7 + size)
        sendbuf[7 + size] = sum.toByte()
        sendbuf[8 + size] = (sum shr 8).toByte()
        mIsWork = true
        timeOutStart()
        mDeviceCmd = cmdid
        mCmdSize = 0
        mChatService?.write(sendbuf)
        when (sendbuf[4]) {
            ChatsFieldConstants.CMD_CARDSN -> {

            }
            ChatsFieldConstants.CMD_GETIMAGE -> {
                mUpImageSize = 0
            }
        }
    }

    /**
     * stat the timer for counting
     */
    fun timeOutStart() {
        if (mTimerTimeout != null) {
            return
        }
        mTimerTimeout = Timer()
        mHandlerTimeout = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                timeOutStop()
                if (mIsWork) {
                    mIsWork = false
                    //Timber.v("Time Out");
                }
                super.handleMessage(msg)
            }
        }
        mTaskTimeout = object : TimerTask() {
            override fun run() {
                val message = Message()
                message.what = 1
                mHandlerTimeout!!.sendMessage(message)
            }
        }
        mTimerTimeout!!.schedule(mTaskTimeout, 10000, 10000)
    }

    /**
     * stop the timer
     */
    fun timeOutStop() {
        if (mTimerTimeout != null) {
            if (isAdded) {
            }
            mTimerTimeout!!.cancel()
            mTimerTimeout = null
            mTaskTimeout!!.cancel()
            mTaskTimeout = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ChatsFieldConstants.CONNECTION_CODE ->                 // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    val address = data!!.extras!!.getString(EXTRA_DEVICE_ADDRESS)
                    // Get the BLuetoothDevice object
                    val device = mBluetoothAdapter!!.getRemoteDevice(address)
                    Timber.v(address)
                    requireContext().toast("Connecting Device...Please Wait")
                    // Attempt to connect to the device
                    mChatService!!.connect(device)
                }
            ChatsFieldConstants.REQUEST_ENABLE_BT ->                 // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat()
                } else {
                    // User did not enable Bluetooth or an error occured
                    Timber.d("BT not enabled")
                    Toast.makeText(requireContext(), "BT not enabled", Toast.LENGTH_SHORT).show()

                }
        }
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

        setFragmentResult(FRAGMENT_NFC_RESULT_LISTENER, bundleOf(NFC_BUNDLE_KEY to true))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mChatService != null) {
            mChatService!!.stop()
        }
    }
}

const val emailBundle = "Email"
