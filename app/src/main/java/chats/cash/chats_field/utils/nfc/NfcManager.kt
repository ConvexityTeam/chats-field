package chats.cash.chats_field.utils.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcF
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import chats.cash.chats_field.views.auth.AuthActivity
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*


@Suppress("DEPRECATION")
@OptIn(InternalCoroutinesApi::class)
class NfcManager(val context:FragmentActivity): NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null

    var onSuccess: () -> Unit = {}
    var onSuccessFullRead: (String) -> Unit = {}
    var onError: () -> Unit = {}
    var onErrorReading: (String) -> Unit = {}
    var onRead: (String) -> Unit = { }
    var ndefMessage: String? = null
    var scanMode = false

    init {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    }

    fun isNFCSupported(): Boolean {
        return nfcAdapter != null
    }

    fun isNfcEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    fun goToEnableNfcSettings() {
        context.startActivity(Intent(NFC_SETTINGS));
    }

    fun onResume() {

        // Work around for some broken Nfc firmware implementations that poll the card too fast
        val options = Bundle()
        // Work around for some broken Nfc firmware implementations that poll the card too fast
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        // Enable ReaderMode for all types of card and disable platform sounds

        // Enable ReaderMode for all types of card and disable platform sounds
        nfcAdapter?.enableReaderMode(
            context,
            this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            options
        )
    }

    fun onPause() {
        nfcAdapter?.disableReaderMode(context)
    }

    private fun enableForegroundDispatch() {
        val intent = Intent(context, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf(arrayOf<String>(NfcF::class.java.name))
        filters[0] = IntentFilter()
        with(filters[0]) {
            this?.addAction(NfcAdapter.ACTION_TAG_DISCOVERED)
            this?.addCategory(Intent.CATEGORY_DEFAULT)
            try {
                this?.addDataType("*/*")
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                ex.printStackTrace()
                throw RuntimeException(ex)
            }
        }
        nfcAdapter?.enableForegroundDispatch(context, pendingIntent, filters, techList)
    }

    private fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(context)
    }

    fun createTextRecord(payload: String, locale: Locale, encodeInUtf8: Boolean): NdefRecord {
        Timber.d("creating")
        val langBytes = locale.language.toByteArray(Charset.forName("US-ASCII"))
        val utfEncoding = if (encodeInUtf8) Charset.forName("UTF-8") else Charset.forName("UTF-16")
        val textBytes = payload.toByteArray(utfEncoding)
        val utfBit: Int = if (encodeInUtf8) 0 else 1 shl 7
        val status = (utfBit + langBytes.size).toChar()
        val data = ByteArray(1 + langBytes.size + textBytes.size)
        data[0] = status.code.toByte()
        System.arraycopy(langBytes, 0, data, 1, langBytes.size)
        System.arraycopy(textBytes, 0, data, 1 + langBytes.size, textBytes.size)
        Timber.d(data.toString())
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), data)
    }

    fun writeToTag(
        tag: Tag?,
       onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        try {
            Timber.d("started writing")
            tag?.let {
                val nfcA = MifareClassic.get(tag)
                if (nfcA != null) {

                    nfcA.connect()
                    val authB: Boolean = nfcA.authenticateSectorWithKeyB(2, MifareClassic.KEY_DEFAULT)

                    if(authB) {
                        Timber.d(ndefMessage.toString())

                        val bWrite = ByteArray(16)
                        if(ndefMessage!!.length<16) {
                            val dataToSend: ByteArray =
                                ndefMessage!!.toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend, 0, bWrite, 0, dataToSend.size)

                            nfcA.writeBlock(8, bWrite)
                        }
                        else{
                            val bWrite2 = ByteArray(16)
                            val first = ndefMessage!!.substring(0,15)
                            val second = ndefMessage!!.substring(15,ndefMessage!!.length)
                            val dataToSend: ByteArray =
                                first.toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend, 0, bWrite, 0, dataToSend.size)

                            val dataToSend2: ByteArray =
                                second.toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend2, 0, bWrite2, 0, dataToSend2.size)

                            nfcA.writeBlock(8, bWrite)
                            nfcA.writeBlock(9, bWrite2)
                        }
                        Timber.d("ndef writable success")
                        val bRead: ByteArray = nfcA.readBlock(8)
                        val str = String(bRead, StandardCharsets.UTF_8)
                        Timber.d("ndef read success $str")
                        nfcA.close()
                        onSuccess()
                    }
                    else{
                        Timber.d("Not authenticated")
                    }
                } else {
                    onError()
                }
            }
            if (tag == null) {
                onError()
            }

        } catch (e: Exception) {
            onError()
            e.printStackTrace()
        }
    }



    fun readToTag(
        tag: Tag?,
        onSuccess: (String) -> Unit,
        onError: () -> Unit,
    ) {

        try {
            Timber.d("started reading")
            tag?.let {
                Timber.d("tag read success")
                val nfcA = MifareClassic.get(tag)
                if (nfcA != null) {
                    Timber.d("ndef read success")
                    nfcA.connect()
                    val authB: Boolean = nfcA.authenticateSectorWithKeyB(2, MifareClassic.KEY_DEFAULT)
                    if(authB) {
                        val bRead: ByteArray = nfcA.readBlock(8)
                        val bRead2: ByteArray = nfcA.readBlock(9)

                        val str = String(bRead, StandardCharsets.UTF_8)
                        val str2 =  if(bRead2.isEmpty()) "" else String(bRead2, StandardCharsets.UTF_8)
                        val combinedString = (str+str2).replace("?","")
                        Timber.d("ndef read success $combinedString")
                        nfcA.close()
                        onSuccess(combinedString)
                    }
                    else{
                        Timber.d("Not authenticated")
                    }
                } else {
                    onError()
                }
            }
            if (tag == null) {
                onError()
            }

        } catch (e: Exception) {
            onError()
            e.printStackTrace()
        }
    }



    override fun onTagDiscovered(tag: Tag?) {

        if (scanMode) {
            writeToTag(tag, onSuccess, onError)
        }
        else{
            readToTag(tag, onSuccessFullRead, onError)
        }

    }
}

const val NFC_SETTINGS = "android.settings.NFC_SETTINGS"