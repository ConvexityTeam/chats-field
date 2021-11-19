package com.codose.chats.views.auth.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager2.widget.ViewPager2
import com.codose.chats.R
import com.codose.chats.utils.*
import com.codose.chats.utils.BluetoothConstants.CMD_CARDSN
import com.codose.chats.utils.BluetoothConstants.EXTRA_DEVICE_ADDRESS
import com.codose.chats.utils.BluetoothConstants.mBat
import com.codose.chats.utils.FingerPrintUtils.getFingerprintImage
import com.codose.chats.utils.FingerPrintUtils.memcpy
import com.codose.chats.views.auth.adapter.PrintPager
import com.codose.chats.views.auth.adapter.PrintPagerAdapter
import com.codose.chats.views.auth.dialog.DeviceSelectorDialog
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.codose.chats.views.base.BaseFragment
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_register_print.*
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.internal.and
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

@InternalCoroutinesApi
class RegisterPrintFragment : BaseFragment() {

    private val TAG = "BluetoothReader"
    private val allFinger = arrayListOf<Bitmap>()

    val IMG360 = 360
    val IMG288 = 288

    val captureFingers = arrayListOf(
        "left thumb",
        "left index",
        "left little",
        "right thumb",
        "right index",
        "right little"
    )

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


    var mCardSn = ByteArray(7)

    var mUpImage = ByteArray(73728) // image data

    var mUpImageSize = 0
    var mUpImageCount = 0
    private var currentPosition = 0
    private var imgSize = 0
    private var isConnected = false
    var printPager = ArrayList<PrintPager>()

    private lateinit var adapter : PrintPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //checking the permission
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                BluetoothConstants.PERMISSIONS_STORAGE,
                BluetoothConstants.REQUEST_PERMISSION_CODE)
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_print, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scannerProgress.progress = 0
        adapter = PrintPagerAdapter(requireContext())
        printPager = ArrayList()
        printPager.add(PrintPager(R.drawable.ic_fingerprint_scan, null))
        print_view_pager.adapter = adapter
        adapter.submitList(printPager)
        print_view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                if (isConnected) {
                    scannerProgress.progress = currentPosition + 1
                    registerPrintNextBtn.text = "Capture ${captureFingers[position]} finger"
                }
            }
        })
        setUpData()
    }

    private fun setUpData() {
        registerPrintBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        // The Handler that gets information back from the BluetoothChatService
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, BluetoothConstants.REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat()
        }
        registerPrintNextBtn.setOnClickListener {
            openDeviceSelector()
        }
    }

    private fun openDeviceSelector() {
        val bottomSheetDialogFragment = DeviceSelectorDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = true
        bottomSheetDialogFragment.setTargetFragment(this, BluetoothConstants.CONNECTION_CODE)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),
            "BottomSheetDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BluetoothConstants.CONNECTION_CODE ->                 // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    val address = data!!.extras!!.getString(EXTRA_DEVICE_ADDRESS)
                    // Get the BLuetoothDevice object
                    val device = mBluetoothAdapter!!.getRemoteDevice(address)
                    Timber.v(address)
                    showToast("Connecting Device...Please Wait")
                    // Attempt to connect to the device
                    mChatService!!.connect(device)
                }
            BluetoothConstants.REQUEST_ENABLE_BT ->                 // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat()
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled")
                    Toast.makeText(requireContext(), "BT not enabled", Toast.LENGTH_SHORT).show()

                }
        }
    }

    var getBattery = true

    private fun setupChat() {
        @SuppressLint("HandlerLeak") val mHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    BluetoothConstants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                        BluetoothReaderService.STATE_CONNECTED -> {

                        }
                        BluetoothReaderService.STATE_CONNECTING -> {

                        }
                        BluetoothReaderService.STATE_LISTEN, BluetoothReaderService.STATE_NONE -> {

                        }
                    }
                    BluetoothConstants.MESSAGE_WRITE -> {

                    }
                    BluetoothConstants.MESSAGE_READ -> {
                        val readBuf = msg.obj as ByteArray
                        if (readBuf.isNotEmpty()) {
                            if (readBuf[0] == 0x1b.toByte()) {
                                //
                            } else {
                                if(getBattery){
                                    ReceiveBatteryCommand(readBuf, msg.arg1)
                                }else{
                                    ReceiveCommand(readBuf, msg.arg1)
                                }

                            }
                        }
                    }
                    BluetoothConstants.MESSAGE_DEVICE_NAME -> {
                        // save the connected device's name
                        mConnectedDeviceName = msg.data.getString(BluetoothConstants.DEVICE_NAME)
                        Toast.makeText(requireContext(),
                            "Connected to $mConnectedDeviceName",
                            Toast.LENGTH_SHORT).show()
                        SendCommand(BluetoothConstants.CMD_GETBAT, null, 0)

                        isConnected = true
                        registerPrintNextBtn.text = "Capture ${captureFingers[0]} finger"
                        scannerProgress.progress = 1
                        deviceConnectedText.text = "Connected to $mConnectedDeviceName"
                        registerPrintNextBtn.setOnClickListener {
                            showToast("Place ${captureFingers[currentPosition]} on fingerprint scanner")
                            printProgress.show()
                            imgSize = IMG288
                            mUpImageSize = 0
                            SendCommand(BluetoothConstants.CMD_GETIMAGE, null, 0)
                        }
                    }
                    BluetoothConstants.MESSAGE_TOAST -> {
                        try {
                            Toast.makeText(requireContext(),
                                msg.data.getString(
                                    BluetoothConstants.TOAST),
                                Toast.LENGTH_SHORT).show()
                        } catch (e: Throwable) {
                            Timber.v(e)
                        }

                    }
                }
            }
        }
        mChatService = BluetoothReaderService(requireContext(), mHandler) // Initialize the BluetoothChatService to perform bluetooth connections

        mOutStringBuffer = StringBuffer("")

    }

    private fun ReceiveCommand(databuf: ByteArray, datasize: Int) {
        if (mDeviceCmd == BluetoothConstants.CMD_GETIMAGE) { //receiving the image data from the device
            if (imgSize == BluetoothConstants.IMG288) {
//                printProgress.show() //image size with 256*360
                memcpy(mUpImage, mUpImageSize, databuf, 0, datasize)
                mUpImageSize = mUpImageSize + datasize
                //Timber.v("Image Len="+Integer.toString(mUpImageSize)+"--"+Integer.toString(mUpImageCount));
                if (mUpImageSize >= 36864) {
                    val file = File("/sdcard/test.raw")
                    try {
                        file.createNewFile()
                        val out = FileOutputStream(file)
                        out.write(mUpImage)
                        out.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val bmpdata = getFingerprintImage(mUpImage, 256, 288, 0 /*18*/)
                    if(bmpdata !=null){
                        val image = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.size)
                        printProgress.hide()
                        try {
                            allFinger[currentPosition] = image
                        }catch (t: Throwable){
                            allFinger.add(image)
                        }

                        if(allFinger.size >= 6){
                            printPager[currentPosition] =
                                PrintPager(R.drawable.ic_fingerprint_scan, image)
                            adapter.submitList(printPager)
                            adapter.notifyDataSetChanged()
                            verifyPrintDoneBtn.show()
                            mViewModel.allFinger = allFinger
                            verifyPrintDoneBtn.setOnClickListener{
                                findNavController().navigateUp()
                            }
                        }else{
                            scannerProgress.progress = currentPosition + 1
                            try {
                                printPager[currentPosition] =
                                    PrintPager(R.drawable.ic_fingerprint_scan, image)
                                if(printPager.size == currentPosition + 1){
                                    printPager.add(PrintPager(R.drawable.ic_fingerprint_scan, null))
                                }
                                adapter.submitList(printPager)
                                adapter.notifyDataSetChanged()
                                print_view_pager.setCurrentItem(currentPosition + 1, true)
                            }catch (t: Throwable){

                            }

                        }
//                        saveJPGimage(image)
                        printProgress.hide()
                    }else{
                        Sentry.captureMessage("The Bitmap is Null")
                    }
                    printProgress.hide()

                    val inpdata = ByteArray(73728)
                    val inpsize = 73728
                    System.arraycopy(bmpdata, 1078, inpdata, 0, inpsize)
//                    SaveWsqFile(inpdata, inpsize, "fingerprint.wsq")
//                    Log.d(BluetoothConstants.TAG, "bmpdata.length:" + bmpdata.size)
                    mUpImageSize = 0
                    mUpImageCount = mUpImageCount + 1
                    mIsWork = false
                    Timber.v("Display Image")
                }
            }
        } else {
            //other data received from the device
            // append the databuf received into mCmdData.
            memcpy(mCmdData, mCmdSize, databuf, 0, datasize)
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
                        printProgress.hide()
                        memcpy(mCardSn, 0, mCmdData, 8, size)
                        val cardUID = Integer.toHexString(mCardSn[0] and 0xFF) + Integer.toHexString(
                            mCardSn[1] and 0xFF) + Integer.toHexString(mCardSn[2] and 0xFF) + Integer.toHexString(
                            mCardSn[3] and 0xFF) + Integer.toHexString(mCardSn[4] and 0xFF) + Integer.toHexString(
                            mCardSn[5] and 0xFF) + Integer.toHexString(mCardSn[6] and 0xFF)
                        showToast("NFC Card scan successfully")

                        Timber.v("Read Card SN Succeed:" + cardUID)
                    } else {
                        Timber.v("Search Fail")
                        showToast("Unable to scan card")
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
    }

    private fun ReceiveBatteryCommand(databuf: ByteArray, datasize: Int) {

        //other data received from the device
        // append the databuf received into mCmdData.
        memcpy(mCmdData, mCmdSize, databuf, 0, datasize)
        mCmdSize = mCmdSize + datasize
        val totalsize: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00) + 9
        if (mCmdSize >= totalsize) {
            mCmdSize = 0
            mIsWork = false
            timeOutStop()
            //parsing the mCmdData
            val size: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00) - 1
            if (size > 0) {
                memcpy(mBat, 0, mCmdData, 8, size)
                val batVal: Double = mBat.get(0) / 10.0
                val batPercent = (batVal - 3.45) / 0.75 * 100
                val decimalFormat = DecimalFormat("0.00")
                val batPercentage = decimalFormat.format(batPercent) + " %"
                deviceConnectedText.text =
                    "Connected to $mConnectedDeviceName" + "\nBattery Percentage:$batPercentage"
            }
        }
        getBattery = false
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
        if (size > 0 && data!=null) {
            for (i in 0 until size) {
                sendbuf[7 + i] = data[i]
            }
        }
        val sum: Int = BluetoothCommands.calcCheckSum(sendbuf, 7 + size)
        sendbuf[7 + size] = sum.toByte()
        sendbuf[8 + size] = (sum shr 8).toByte()
        mIsWork = true
        timeOutStart()
        mDeviceCmd = cmdid
        mCmdSize = 0
        mChatService?.write(sendbuf)
        when (sendbuf[4]) {
            CMD_CARDSN -> {

            }
            BluetoothConstants.CMD_GETIMAGE -> {
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
            if(isAdded){
                printProgress.hide()
            }
            mTimerTimeout!!.cancel()
            mTimerTimeout = null
            mTaskTimeout!!.cancel()
            mTaskTimeout = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mChatService!=null){
            mChatService!!.stop()
        }
    }
}

//dev-aminu.xyz