package chats.cash.chats_field.utils.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcF
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import chats.cash.chats_field.views.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.Charset
import java.util.*


@Suppress("DEPRECATION")
@OptIn(InternalCoroutinesApi::class)
class NfcManager(val context:FragmentActivity): NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null

    var onSuccess:() -> Unit ={}
    var onError:() -> Unit ={}
    var onErrorReading:(String) -> Unit ={}
    var onRead:(String)->Unit={ }
    var ndefMessage: NdefMessage? = null
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
        ndefMessage: NdefMessage, onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        try {
            Timber.d("started writing")

            tag?.let {
                Timber.d("tag read success")
                val Ndef = Ndef.get(tag)
                if(Ndef!=null){
                    Timber.d("ndef read success")
                    Ndef.connect()
                    if (Ndef.isWritable) {
                        Timber.d("ndef writable success")
                        Ndef.writeNdefMessage(ndefMessage)

                        Ndef.close()
                        onSuccess()
                    } else {
                        Timber.d("ndef read failed")
                        onError()
                    }
                }
                else {
                    Timber.d("ndef read not success")
                    val ndefFormatable = NdefFormatable.get(tag)
                    ndefFormatable?.let {
                        Timber.d("formatting read success")
                        ndefFormatable.connect()
                        ndefFormatable.format(ndefMessage)
                        ndefFormatable.close()
                        try {
                            val notification: Uri =
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            val r = RingtoneManager.getRingtone(
                                context,
                                notification
                            )
                            r.play()
                            CoroutineScope(Main).launch {
                                delay(1000)
                                r.stop()
                            }
                        } catch (e: java.lang.Exception) {
                            // Some error playing sound
                        }
                        onSuccess()
                    } ?: {
                        onError()
                    }
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
        Timber.v(tag?.techList.toString())
        ndefMessage?.let {
            if(scanMode) {
                writeToTag(tag, it, onSuccess, onError)
            }
            else{
                val ndef = Ndef.get(tag)
                ndef.connect()
                val ndefMessages = ndef.ndefMessage
                ndefMessages?.let {
                    if(it.records.isNullOrEmpty()){
                        onErrorReading("No Records")
                    }
                    else {
                        val record = it.records[0]
                        onRead(record.payload.toString())
                    }
                }?:{
                    onErrorReading("No NDef Messages")
                }
            }
        }
    }
}

const val NFC_SETTINGS = "android.settings.NFC_SETTINGS"