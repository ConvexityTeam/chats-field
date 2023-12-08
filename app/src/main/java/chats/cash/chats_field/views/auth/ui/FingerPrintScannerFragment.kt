package chats.cash.chats_field.views.auth.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import chats.cash.chats_field.databinding.FragmentFingerPrintScannerBinding
import chats.cash.chats_field.utils.ChatsFieldCommands
import chats.cash.chats_field.utils.ChatsFieldConstants
import chats.cash.chats_field.utils.ChatsFieldConstants.CMD_GETIMAGE
import chats.cash.chats_field.utils.ChatsFieldConstants.CONNECTION_CODE
import chats.cash.chats_field.utils.ChatsFieldConstants.DEVICE_NAME
import chats.cash.chats_field.utils.ChatsFieldConstants.EXTRA_DEVICE_ADDRESS
import chats.cash.chats_field.utils.ChatsFieldConstants.MESSAGE_DEVICE_NAME
import chats.cash.chats_field.utils.ChatsFieldConstants.MESSAGE_READ
import chats.cash.chats_field.utils.ChatsFieldConstants.MESSAGE_STATE_CHANGE
import chats.cash.chats_field.utils.ChatsFieldConstants.MESSAGE_TOAST
import chats.cash.chats_field.utils.ChatsFieldConstants.MESSAGE_WRITE
import chats.cash.chats_field.utils.ChatsFieldConstants.PERMISSIONS_STORAGE
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUEST_ENABLE_BT
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUEST_PERMISSION_CODE
import chats.cash.chats_field.views.auth.dialog.DeviceSelectorDialog
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.internal.and
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.experimental.or

@InternalCoroutinesApi
class FingerPrintScannerFragment : Fragment() {
    private var sDirectory = ""
    private val TAG = "BluetoothReader"

    // default image size
    val IMG_WIDTH = 256
    val IMG_HEIGHT = 288

    val IMG360 = 360

    private var mDeviceCmd: Byte = 0x00
    private var mIsWork = false
    private var mCmdData = ByteArray(10240)
    private var mCmdSize = 0

    private var mTimerTimeout: Timer? = null
    private var mTaskTimeout: TimerTask? = null
    private var mHandlerTimeout: Handler? = null

    // Name of the connected device
    private var mConnectedDeviceName: String? = null

    // Array adapter for the conversation thread
    private val mConversationArrayAdapter: ArrayAdapter<String>? = null

    // String buffer for outgoing messages
    private var mOutStringBuffer: StringBuffer? = null

    // Local Bluetooth adapter
    private var mBluetoothAdapter: BluetoothAdapter? = null

    // Member object for the chat services
    private var mChatService: chats.cash.chats_field.utils.BluetoothReaderService? = null

    // definition of variables which used for storing the fingerprint template
    var mRefData = ByteArray(512) // enrolled FP template data

    var mRefSize = 0
    var mMatData = ByteArray(512) // match FP template data

    var mMatSize = 0

    var mCardSn = ByteArray(7)
    var mCardData = ByteArray(4096)
    var mCardSize = 0

    var mUpImage = ByteArray(73728) // image data

    var mUpImageSize = 0
    var mUpImageCount = 0

    private var imgSize = 0

    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // checking the permission

        // checking the permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                PERMISSIONS_STORAGE,
                REQUEST_PERMISSION_CODE,
            )
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private lateinit var binding: FragmentFingerPrintScannerBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFingerPrintScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The Handler that gets information back from the BluetoothChatService
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat()
        }
    }

    private fun setupChat() {
        @SuppressLint("HandlerLeak")
        val mHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                        chats.cash.chats_field.utils.BluetoothReaderService.STATE_CONNECTED -> {
                        }
                        chats.cash.chats_field.utils.BluetoothReaderService.STATE_CONNECTING -> {
                        }
                        chats.cash.chats_field.utils.BluetoothReaderService.STATE_LISTEN, chats.cash.chats_field.utils.BluetoothReaderService.STATE_NONE -> {
                        }
                    }
                    MESSAGE_WRITE -> {
                    }
                    MESSAGE_READ -> {
                        val readBuf = msg.obj as ByteArray
                        if (readBuf.size > 0) {
                            if (readBuf[0] == 0x1b.toByte()) {
                                //
                            } else {
                                ReceiveCommand(readBuf, msg.arg1)
                            }
                        }
                    }
                    MESSAGE_DEVICE_NAME -> {
                        // save the connected device's name
                        mConnectedDeviceName = msg.data.getString(DEVICE_NAME)
                        Toast.makeText(
                            requireContext(),
                            "Connected to $mConnectedDeviceName",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    MESSAGE_TOAST -> Toast.makeText(
                        requireContext(),
                        msg.data.getString(
                            ChatsFieldConstants.TOAST,
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
        mChatService = chats.cash.chats_field.utils.BluetoothReaderService(
            requireContext(),
            mHandler,
        ) // Initialize the BluetoothChatService to perform bluetooth connections

        mOutStringBuffer = StringBuffer("")

        binding.captureBtn.setOnClickListener {
            imgSize = IMG360
            mUpImageSize = 0
            SendCommand(CMD_GETIMAGE, null, 0)
        }

        binding.selectDevice.setOnClickListener {
            openDeviceSelector()
        }
    }

    fun SendCommand(cmdid: Byte, data: ByteArray?, size: Int) {
        if (mIsWork) return
        val sendsize = 9 + size
        val sendbuf = ByteArray(sendsize)
        sendbuf[0] = 'F'.code.toByte()
        sendbuf[1] = 'T'.code.toByte()
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
            ChatsFieldConstants.CMD_PASSWORD -> {
            }
            ChatsFieldConstants.CMD_ENROLID -> {
            }
            ChatsFieldConstants.CMD_VERIFY -> {
            }
            ChatsFieldConstants.CMD_IDENTIFY -> {
            }
            ChatsFieldConstants.CMD_DELETEID -> {
            }
            ChatsFieldConstants.CMD_CLEARID -> {
            }
            ChatsFieldConstants.CMD_ENROLHOST -> {
            }
            ChatsFieldConstants.CMD_CAPTUREHOST -> {
            }
            ChatsFieldConstants.CMD_MATCH -> {
            }
            ChatsFieldConstants.CMD_WRITEFPCARD, ChatsFieldConstants.CMD_WRITEDATACARD -> {
            }
            ChatsFieldConstants.CMD_READFPCARD, ChatsFieldConstants.CMD_READDATACARD -> {
            }
            ChatsFieldConstants.CMD_FPCARDMATCH -> {
            }
            ChatsFieldConstants.CMD_CARDSN -> {
            }
            ChatsFieldConstants.CMD_GETSN -> {
            }
            ChatsFieldConstants.CMD_GETBAT -> {
            }
            CMD_GETIMAGE -> {
                mUpImageSize = 0
            }
            ChatsFieldConstants.CMD_GETCHAR -> {
            }
            ChatsFieldConstants.CMD_GET_VERSION -> {
            }
        }
    }

    fun CreateDirectory() {
        sDirectory = Environment.getExternalStorageDirectory().toString() + "/Fingerprint Images/"
        val destDir = File(sDirectory)
        if (!destDir.exists()) {
            destDir.mkdirs()
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
                    // Timber.v("Time Out");
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
            mTimerTimeout!!.cancel()
            mTimerTimeout = null
            mTaskTimeout!!.cancel()
            mTaskTimeout = null
        }
    }

    private fun ReceiveCommand(databuf: ByteArray, datasize: Int) {
        if (mDeviceCmd == CMD_GETIMAGE) { // receiving the image data from the device
            if (imgSize == ChatsFieldConstants.IMG200) { // image size with 152*200
                memcpy(mUpImage, mUpImageSize, databuf, 0, datasize)
                mUpImageSize = mUpImageSize + datasize
                if (mUpImageSize >= 15200) {
                    val file = File("/sdcard/test.raw")
                    try {
                        file.createNewFile()
                        val out = FileOutputStream(file)
                        out.write(mUpImage)
                        out.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val bmpdata: ByteArray? = getFingerprintImage(mUpImage, 152, 200, 0 /*18*/)
                    if (bmpdata != null) {
                        val image = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.size)
                        saveJPGimage(image)
                        Log.d(ChatsFieldConstants.TAG, "bmpdata.length:" + bmpdata.size)
                        binding.fingerprintImage.setImageBitmap(image)
                    }
                    mUpImageSize = 0
                    mUpImageCount = mUpImageCount + 1
                    mIsWork = false
                    Timber.v("Display Image")
                }
            } else if (imgSize == ChatsFieldConstants.IMG288) { // image size with 256*288
                memcpy(mUpImage, mUpImageSize, databuf, 0, datasize)
                mUpImageSize = mUpImageSize + datasize
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
                    if (bmpdata != null) {
                        val image = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.size)
                        saveJPGimage(image)
                        binding.fingerprintImage.setImageBitmap(image)
                    }

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
            } else if (imgSize == ChatsFieldConstants.IMG360) { // image size with 256*360
                memcpy(mUpImage, mUpImageSize, databuf, 0, datasize)
                mUpImageSize = mUpImageSize + datasize
                // Timber.v("Image Len="+Integer.toString(mUpImageSize)+"--"+Integer.toString(mUpImageCount));
                if (mUpImageSize >= 46080) {
                    val file = File("/sdcard/test.raw")
                    try {
                        file.createNewFile()
                        val out = FileOutputStream(file)
                        out.write(mUpImage)
                        out.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val bmpdata = getFingerprintImage(mUpImage, 256, 360, 0 /*18*/)
//                    textSize.setText("256 * 360")
                    if (bmpdata != null) {
                        val image = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.size)
                        saveJPGimage(image)
                        binding.fingerprintImage.setImageBitmap(image)
                    }

                    val inpdata = ByteArray(92160)
                    val inpsize = 92160
                    System.arraycopy(bmpdata, 1078, inpdata, 0, inpsize)
//                    SaveWsqFile(inpdata, inpsize, "fingerprint.wsq")
//                    Log.d(BluetoothConstants.TAG, "bmpdata.length:" + bmpdata.size)
                    mUpImageSize = 0
                    mUpImageCount = mUpImageCount + 1
                    mIsWork = false
                    Timber.v("Display Image")
                }

                /*     File f = new File("/sdcard/fingerprint.png");
                if (f.exists()) {
                    f.delete();
                }
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    image.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] inpdata=new byte[73728];
                int inpsize=73728;
                System.arraycopy(bmpdata,1078, inpdata, 0, inpsize);
                SaveWsqFile(inpdata,inpsize,"fingerprint.wsq");*/
            }
        } else { // other data received from the device
            // append the databuf received into mCmdData.
            memcpy(mCmdData, mCmdSize, databuf, 0, datasize)
            mCmdSize = mCmdSize + datasize
            val totalsize: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00) + 9
            if (mCmdSize >= totalsize) {
                mCmdSize = 0
                mIsWork = false
                timeOutStop()

                // parsing the mCmdData
                if (mCmdData[0] == 'F'.code.toByte() && mCmdData[1] == 'T'.code.toByte()) {
                    when (mCmdData[4]) {
                        ChatsFieldConstants.CMD_PASSWORD -> {
                        }
                        ChatsFieldConstants.CMD_ENROLID -> {
                            if (mCmdData[7] == 1.toByte()) {
                                // int id=mCmdData[8]+(mCmdData[9]<<8);
                                val id = mCmdData[8] + (mCmdData[9].toInt() shl 8 and 0xFF00)
                                Timber.v("Enrol Succeed:$id")
                                Log.d(ChatsFieldConstants.TAG, id.toString())
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                        ChatsFieldConstants.CMD_ENROLHOST -> {
                            val size: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00) - 1
                            if (mCmdData[7] == 1.toByte()) {
                                memcpy(mRefData, 0, mCmdData, 8, size)
                                mRefSize = size
                                Timber.v("Enrol Succeed with finger: $userId")
                                userId += 1
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                        ChatsFieldConstants.CMD_CAPTUREHOST -> {
                            val size: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00) - 1
                            if (mCmdData[7] == 1.toByte()) {
                                memcpy(mMatData, 0, mCmdData, 8, size)
                                mMatSize = size
                                var matchFlag = false
//                                while (cursor.moveToNext()) {
//                                    val id =
//                                        cursor.getInt(cursor.getColumnIndex(DBHelper.TABLE_USER_ID))
//                                    val enrol1 =
//                                        cursor.getBlob(cursor.getColumnIndex(DBHelper.TABLE_USER_ENROL1))
//                                    val ret: Int = FPMatch.getInstance().MatchFingerData(enrol1,
//                                        mMatData)
//                                    if (ret > 70) {
//                                        Timber.v("Match OK,Finger = $id!!")
//                                        matchFlag = true
//                                        break
//                                    }
//                                }
                                if (!matchFlag) {
                                    Timber.v("Match Fail !!")
                                }
//                                if (cursor.count == 0) {
//                                    Timber.v("Match Fail !!")
//                                }
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                        ChatsFieldConstants.CMD_MATCH -> {
                            val score: Int = mCmdData[8] + (mCmdData[9].toInt() shl 8 and 0xFF00)
                            if (mCmdData[7] == 1.toByte()) {
                                Timber.v("Match Succeed:$score")
                            } else {
                                Timber.v(
                                    "Search Fail",
                                )
                            }
                        }
                        ChatsFieldConstants.CMD_WRITEFPCARD -> {
                            if (mCmdData[7] == 1.toByte()) {
                                Timber.v(
                                    "Write Fingerprint Card Succeed",
                                )
                            } else {
                                Timber.v(
                                    "Search Fail",
                                )
                            }
                        }
                        ChatsFieldConstants.CMD_READFPCARD -> {
                            val size: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00)
                            if (size > 0) {
                                memcpy(mCardData, 0, mCmdData, 8, size)
                                mCardSize = size
                                Timber.v("Read Fingerprint Card Succeed")
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                        ChatsFieldConstants.CMD_FPCARDMATCH -> {
                            if (mCmdData[7] == 1.toByte()) {
                                Timber.v("Fingerprint Match Succeed")
                                val size: Int =
                                    mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00) - 1
                                val tmpbuf = ByteArray(size)
                                memcpy(tmpbuf, 0, mCmdData, 8, size)
                                Timber.v("Len=$size")
                                val txt = String(tmpbuf)
                                Timber.v(txt)
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                        ChatsFieldConstants.CMD_UPCARDSN, ChatsFieldConstants.CMD_CARDSN -> {
                            val size: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xF0) - 1
                            if (size > 0) {
                                memcpy(mCardSn, 0, mCmdData, 8, size)
                                Timber.v(
                                    "Read Card SN Succeed:" + Integer.toHexString(
                                        mCardSn[0] and 0xFF,
                                    ) + Integer.toHexString(
                                        mCardSn[1] and 0xFF,
                                    ) + Integer.toHexString(mCardSn[2] and 0xFF) + Integer.toHexString(
                                        mCardSn[3] and 0xFF,
                                    ) + Integer.toHexString(mCardSn[4] and 0xFF) + Integer.toHexString(
                                        mCardSn[5] and 0xFF,
                                    ) + Integer.toHexString(mCardSn[6] and 0xFF),
                                )
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                        ChatsFieldConstants.CMD_WRITEDATACARD -> {
                            if (mCmdData[7] == 1.toByte()) {
                                Timber.v("Write Card Data Succeed")
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                        ChatsFieldConstants.CMD_READDATACARD -> {
                            val size: Int = mCmdData[5] + (mCmdData[6].toInt() shl 8 and 0xFF00)
                            if (size > 0) {
                                memcpy(mCardData, 0, mCmdData, 8, size)
                                mCardSize = size
//                                Timber.vHex(mCardData, size)
                            } else {
                                Timber.v("Search Fail")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * method of copying the byte[] data with specific length
     * @param dstbuf byte[] for storing the copied data with specific length
     * @param dstoffset the starting point for storing
     * @param srcbuf the source byte[] used for copying.
     * @param srcoffset the starting point for copying
     * @param size the length required to copy
     */
    private fun memcpy(
        dstbuf: ByteArray,
        dstoffset: Int,
        srcbuf: ByteArray,
        srcoffset: Int,
        size: Int,
    ) {
        for (i in 0 until size) {
            dstbuf[dstoffset + i] = srcbuf[srcoffset + i]
        }
    }

    /**
     * calculate the check sum of the byte[]
     * @param buffer byte[] required for calculating
     * @param size the size of the byte[]
     * @return the calculated check sum
     */
    private fun calcCheckSum(buffer: ByteArray, size: Int): Int {
        var sum = 0
        for (i in 0 until size) {
            sum = sum + buffer[i]
        }
        return sum and 0x00ff
    }

    private fun changeByte(data: Int): ByteArray? {
        val b4 = (data shr 24).toByte()
        val b3 = (data shl 8 shr 24).toByte()
        val b2 = (data shl 16 shr 24).toByte()
        val b1 = (data shl 24 shr 24).toByte()
        return byteArrayOf(b1, b2, b3, b4)
    }

    /**
     * generate the image data into Bitmap format
     * @param width width of the image
     * @param height height of the image
     * @param data image data
     * @return bitmap image data
     */
    private fun toBmpByte(width: Int, height: Int, data: ByteArray): ByteArray? {
        var buffer: ByteArray? = null
        try {
            val baos = ByteArrayOutputStream()
            val dos = DataOutputStream(baos)
            val bfType = 0x424d
            val bfSize = 54 + 1024 + width * height
            val bfReserved1 = 0
            val bfReserved2 = 0
            val bfOffBits = 54 + 1024
            dos.writeShort(bfType)
            dos.write(changeByte(bfSize), 0, 4)
            dos.write(changeByte(bfReserved1), 0, 2)
            dos.write(changeByte(bfReserved2), 0, 2)
            dos.write(changeByte(bfOffBits), 0, 4)
            val biSize = 40
            val biPlanes = 1
            val biBitcount = 8
            val biCompression = 0
            val biSizeImage = width * height
            val biXPelsPerMeter = 0
            val biYPelsPerMeter = 0
            val biClrUsed = 256
            val biClrImportant = 0
            dos.write(changeByte(biSize), 0, 4)
            dos.write(changeByte(width), 0, 4)
            dos.write(changeByte(height), 0, 4)
            dos.write(changeByte(biPlanes), 0, 2)
            dos.write(changeByte(biBitcount), 0, 2)
            dos.write(changeByte(biCompression), 0, 4)
            dos.write(changeByte(biSizeImage), 0, 4)
            dos.write(changeByte(biXPelsPerMeter), 0, 4)
            dos.write(changeByte(biYPelsPerMeter), 0, 4)
            dos.write(changeByte(biClrUsed), 0, 4)
            dos.write(changeByte(biClrImportant), 0, 4)
            val palatte = ByteArray(1024)
            for (i in 0..255) {
                palatte[i * 4] = i.toByte()
                palatte[i * 4 + 1] = i.toByte()
                palatte[i * 4 + 2] = i.toByte()
                palatte[i * 4 + 3] = 0
            }
            dos.write(palatte)
            dos.write(data)
            dos.flush()
            buffer = baos.toByteArray()
            dos.close()
            baos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return buffer
    }

    /**
     * generate the fingerprint image
     * @param data image data
     * @param width width of the image
     * @param height height of the image
     * @param offset default setting as 0
     * @return bitmap image data
     */
    fun getFingerprintImage(
        data: ByteArray?,
        width: Int,
        height: Int,
        offset: Int,
    ): ByteArray? {
        if (data == null) {
            return null
        }
        val imageData = ByteArray(width * height)
        for (i in 0 until width * height / 2) {
            imageData[i * 2] = (data[i + offset] and 0xf0).toByte()
            imageData[i * 2 + 1] = (data[i + offset] or 4 and 0xf0).toByte()
        }
        return toBmpByte(width, height, imageData)
    }

    /**
     * method for saving the fingerprint image as JPG
     * @param bitmap bitmap image
     */
    fun saveJPGimage(bitmap: Bitmap) {
        val dir = sDirectory
        val imageFileName = System.currentTimeMillis().toString()
        try {
            val file = File("$dir$imageFileName.jpg")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CONNECTION_CODE -> // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    val address = data!!.extras!!.getString(EXTRA_DEVICE_ADDRESS)
                    // Get the BLuetoothDevice object
                    val device = mBluetoothAdapter!!.getRemoteDevice(address)
                    Timber.v(address)
                    // Attempt to connect to the device
                    mChatService!!.connect(device)
                }
            REQUEST_ENABLE_BT -> // When the request to enable Bluetooth returns
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

    private fun openDeviceSelector() {
        val bottomSheetDialogFragment = DeviceSelectorDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = true
        bottomSheetDialogFragment.setTargetFragment(this, CONNECTION_CODE)
        bottomSheetDialogFragment.show(
            requireFragmentManager().beginTransaction(),
            "BottomSheetDialog",
        )
    }
}
