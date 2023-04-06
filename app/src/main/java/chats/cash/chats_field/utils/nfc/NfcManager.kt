package chats.cash.chats_field.utils.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import chats.cash.chats_field.utils.isDebugMode
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
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
    var userEmail: String? = null
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

    fun writeToTag(
        tag: Tag?,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        try {

            tag?.let {

                //get the Mifareclassic
                val nfcA = MifareClassic.get(tag)

                if (nfcA != null) {

                    //connect to the tag
                    nfcA.connect()

                    //authenticate the tag
                    val authB: Boolean = nfcA.authenticateSectorWithKeyB(2, MifareClassic.KEY_DEFAULT)

                    if(authB) {
                        Timber.v("writing $userEmail")
                        //create an empty byte array to write data
                        val bWrite = ByteArray(16)

                        //if email is less than 16, we can write to only one sector (8) and clear sector 9
                        if(userEmail!!.length<=16) {

                            //get the email and convert it to byte array with UTF8 encoding
                            val dataToSend: ByteArray =
                                userEmail!!.toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend, 0, bWrite, 0, dataToSend.size)

                            //since all data can fit into sector 8, we need to clear sector 9 previous string with empty string,
                            // incase there was data before, it wont show again,

                            val bWrite2 = ByteArray(16)
                            val dataToSend2: ByteArray =
                                "".toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend2, 0, bWrite2, 0, dataToSend2.size)

                            val bWrite3 = ByteArray(16)
                            val dataToSend3: ByteArray =
                                "".toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend3, 0, bWrite2, 0, dataToSend3.size)

                            //writing to blocks
                            nfcA.writeBlock(8, bWrite)
                            nfcA.writeBlock(9,bWrite2)
                            nfcA.writeBlock(10,bWrite3)

                        }

                        //the strings needs two sectors to write, as its length is bigger than 16
                        else{

                            //create another empty byte arrays
                            val bWrite2 = ByteArray(16)
                            //create another empty byte arrays
                            val bWrite3 = ByteArray(16)

                            //split the string into two parts, from 0 ,15 and 15 to length, e.g Simonnnnnnn = simonnnn , nnnnn
                            val first = userEmail!!.substring(0,15)
                            val second = userEmail!!.substring(15,if(userEmail!!.length>32) 31 else userEmail!!.length)
                            val third = if(userEmail!!.length>32) userEmail!!.substring(32,userEmail!!.length) else ""

                            //convert string to byte arrays
                            val dataToSend: ByteArray =
                                first.toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend, 0, bWrite, 0, dataToSend.size)

                            val dataToSend2: ByteArray =
                                second.toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend2, 0, bWrite2, 0, dataToSend2.size)

                            val dataToSend3: ByteArray =
                                third.toByteArray(StandardCharsets.UTF_8)
                            System.arraycopy(dataToSend3, 0, bWrite3, 0, dataToSend3.size)

                            //write ths trings
                            nfcA.writeBlock(8, bWrite)
                            nfcA.writeBlock(9, bWrite2)
                            if(userEmail!!.length>=32) {
                                nfcA.writeBlock(10, bWrite3)
                            }
                        }

                        nfcA.close()
                        onSuccess()
                    }
                    else{
                        Timber.d("Not authenticated")
                    }
                } else {
                    onError()
                    Timber.v("null")
                }
            }
            if (tag == null) {
                Timber.v("tag is null")
                onError()
            }

        } catch (e: Exception) {
            onError()
            Timber.v(e)
            e.printStackTrace()
        }
    }



    fun readToTag(
        tag: Tag?,
        onSuccess: (String) -> Unit,
        onError: () -> Unit,
    ) {

        try {

            tag?.let {

                //getting the tag
                val nfcA = MifareClassic.get(tag)

                if (nfcA != null) {

                    //connecting to the tag
                    nfcA.connect()

                    //authenticating the tag, this returns a boolean
                    val authB: Boolean = nfcA.authenticateSectorWithKeyB(2, MifareClassic.KEY_DEFAULT)

                    if(authB) {

                        //reading block 8
                        val bRead: ByteArray = nfcA.readBlock(8)

                        //reading block 9
                        val bRead2: ByteArray = nfcA.readBlock(9)

                        //reading block 10
                        val bRead3: ByteArray = nfcA.readBlock(10)

                        //convert the byte array to string using UTF_*
                        val str = String(bRead, StandardCharsets.UTF_8)
                        val str2 =  if(bRead2.isEmpty()) "" else String(bRead2, StandardCharsets.UTF_8)
                        val str3 =  if(bRead3.isEmpty()) "" else String(bRead3, StandardCharsets.UTF_8)
                        Timber.v(str3)

                        //combining the two strings, and replace the other blocks which are empty (?) with empty strings ""
                        val combinedString = (str+str2+str3).replace("?","")

                        //close the tag
                        nfcA.close()
                        Timber.v("read from tag $combinedString")
                        //return success
                        onSuccess(combinedString)
                    }
                    else{
                        onError()
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
            userEmail?.let {
                writeToTag(tag, onSuccess, onError)
            }
        }
        else{
            if(isDebugMode()) {
                readToTag(tag, onSuccessFullRead, onError)
            }
        }

    }
}

const val NFC_SETTINGS = "android.settings.NFC_SETTINGS"