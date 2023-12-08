package chats.cash.chats_field.utils.fingerprint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import chats.cash.chats_field.R
import com.fgtit.data.ConversionsEx
import com.fgtit.data.wsq
import com.fgtit.device.Constants
import com.fgtit.device.FPModule
import com.fgtit.fpcore.FPMatch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class FingerPrintManager(
    private val context: Context,
    private var listener: FingerPrintScanCallback? = null,
) {

    private val fpm = FPModule()

    private val bmpdata = ByteArray(Constants.RESBMP_SIZE)
    private var bmpsize = 0

    private val refdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private var refsize = 0

    private val matdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private var matsize = 0

    private var refstring = ""
    private var matstring = ""

    private var worktype = 0

    private var init: Int? = null

    fun initSdk() {
        if (init == null) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog().build(),
            )
            init = fpm.InitMatch()
            fpm.SetContextHandler(context, mHandler)
            fpm.SetTimeOut(Constants.TIMEOUT_LONG)
            fpm.SetLastCheckLift(true)
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            Timber.tag("test-matstring").d("${msg.what} ${msg.arg1}")
            when (msg.what) {
                Constants.FPM_DEVICE -> when (msg.arg1) {
                    Constants.DEV_OK -> listener?.onDeviceMessage(context.getString(R.string.open_device_ok))
                    Constants.DEV_FAIL -> listener?.onDeviceMessage(context.getString(R.string.open_device_fail))
                    Constants.DEV_ATTACHED -> listener?.onDeviceMessage(context.getString(R.string.usb_device_attached))
                    Constants.DEV_DETACHED -> listener?.onDeviceMessage(context.getString(R.string.usb_device_detached))
                    Constants.DEV_CLOSE -> listener?.onDeviceMessage(context.getString(R.string.device_close))
                }

                Constants.FPM_PLACE -> listener?.onDeviceMessage(context.getString(R.string.place_finger))
                Constants.FPM_LIFT -> listener?.onDeviceMessage(context.getString(R.string.lift_finger))
                Constants.FPM_GENCHAR -> {
                    if (msg.arg1 == 1) {
                        if (worktype == 0) {
                            listener?.onDeviceMessage(context.getString(R.string.generate_template_ok))
                            matsize = fpm.GetTemplateByGen(matdata)
                            matstring = ConversionsEx.getInstance().ToAnsiIso(
                                matdata,
                                ConversionsEx.ANSI_378_2004,
                                ConversionsEx.COORD_MIRRORV,
                            )

                            val st = ConversionsEx.getInstance().ToAnsiIso(
                                matdata,
                                ConversionsEx.ISO_19794_2005,
                                ConversionsEx.COORD_MIRRORV,
                            )

                            Base64.encodeToString(matdata, 0)
                            //matstring = ConversionsEx.getInstance().ToAnsiIso(matdata, ConversionsEx.ISO_19794_2005, ConversionsEx.COORD_MIRRORV);
                            Timber.tag("test-matstring").d("handleMessage: Test " + matstring)
                            val sc = MatchIsoTemplateStr(refstring, matstring)
                        } else {
                            listener?.onDeviceMessage(context.getString(R.string.enrolling_template_ok))
                            listener?.onEnrollSuccess()
                            refsize = fpm.GetTemplateByGen(refdata)
                            //显示传感器输出的指纹模板类型
//                            tvFpType.setText(
//                                "raw FP template type: " + ConversionsEx.getInstance()
//                                    .GetDataType(refdata).toString()
//                            )
                            //if(fpm.getDeviceType()==Constants.DEV_7_3G_SPI){
                            refstring = ConversionsEx.getInstance().ToAnsiIso(
                                refdata,
                                ConversionsEx.ANSI_378_2004,
                                ConversionsEx.COORD_MIRRORV,
                            )

                            //if(fpm.getDeviceType()==Constants.DEV_7_3G_SPI){
                            refstring = ConversionsEx.getInstance().ToAnsiIso(
                                refdata,
                                ConversionsEx.ISO_19794_2005,
                                ConversionsEx.COORD_MIRRORV,
                            )

                            refstring = Base64.encodeToString(refdata, 0)
                            Log.d(
                                "test-refstring",
                                "handleMessage: Test $refstring",
                            )
                            //     tvFpData.setText(refstring)
                        }
                    } else {
                        listener?.onDeviceMessage(context.getString(R.string.generate_template_fail))
                    }
                }

                Constants.FPM_NEWIMAGE -> {
                    bmpsize = fpm.GetBmpImage(bmpdata)
                    val bm1 = BitmapFactory.decodeByteArray(bmpdata, 0, bmpsize)
                    listener?.onNewImage(bm1)
                }

                Constants.FPM_TIMEOUT -> listener?.onTimeOut()
            }
        }
    }

    fun MatchIsoTemplateByte(piFeatureA: ByteArray?, piFeatureB: ByteArray?): Int {
        val adat = ByteArray(512)
        val bdat = ByteArray(512)
        val sc = 0

        ConversionsEx.getInstance()
            .AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ANSI_378_2004)
        ConversionsEx.getInstance()
            .AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ANSI_378_2004)
        return FPMatch.getInstance().MatchTemplate(adat, bdat)

        ConversionsEx.getInstance()
            .AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005)
        ConversionsEx.getInstance()
            .AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005)
        return FPMatch.getInstance().MatchTemplate(adat, bdat)

        //如果硬件直接设置为ISO模板，则指纹模块直接返回ISO数据，需要将其转换成私有模板
        /*ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005);
        ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005);
        return FPMatch.getInstance().MatchTemplate(adat, bdat);
        */
        //硬件设置为私有，指纹返回私有格式，直接传入比对函数
        return FPMatch.getInstance().MatchTemplate(piFeatureA, piFeatureB)
        return 0
    }

    fun MatchIsoTemplateStr(strFeatureA: String?, strFeatureB: String?): Int {
        val piFeatureA = Base64.decode(strFeatureA, Base64.DEFAULT)
        val piFeatureB = Base64.decode(strFeatureB, Base64.DEFAULT)
        return MatchIsoTemplateByte(piFeatureA, piFeatureB)
    }

    fun onResume(listener: FingerPrintScanCallback) {
        this.listener = listener
        fpm.ResumeRegister()
        fpm.OpenDevice()
    }

    fun onStop() {
        try {
            fpm.PauseUnRegister()
            fpm.CloseDevice()
            listener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun SaveWsqFile(rawdata: ByteArray?, rawsize: Int, filename: String) {
        val outdata = ByteArray(73728)
        val outsize = IntArray(1)
        wsq.getInstance().RawToWsq(rawdata, rawsize, 256, 288, outdata, outsize, 2.833755f)
        try {
            val fs = File("/sdcard/$filename")
            if (fs.exists()) {
                fs.delete()
            }
            File("/sdcard/$filename")
            val randomFile = RandomAccessFile("/sdcard/$filename", "rw")
            val fileLength = randomFile.length()
            randomFile.seek(fileLength)
            randomFile.write(outdata, 0, outsize[0])
            randomFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun startEnrolling() {
        if (fpm.GenerateTemplate(2)) {
            listener?.onEnroll()
            worktype = 1
        } else {
            listener?.onDeviceMessage(context.getString(R.string.busy))
        }
    }
}

interface FingerPrintScanCallback {

    fun onDeviceMessage(message: String)
    fun onNewImage(image: Bitmap)
    fun onTimeOut()
    fun onEnroll()
    fun onEnrollSuccess()
}
