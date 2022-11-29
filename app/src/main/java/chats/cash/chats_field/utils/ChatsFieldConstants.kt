package chats.cash.chats_field.utils

import android.Manifest
import chats.cash.chats_field.BuildConfig

object ChatsFieldConstants {
    const val sDirectory = ""
    private const val DEBUG_BASE_URL: String = "https://staging-api.chats.cash/v1/"
    private const val RELEASE_BASE_URL: String = "https://api.chats.cash/v1/"
    val BASE_URL =
        if (BuildConfig.BUILD_TYPE.equals("debug", true)) DEBUG_BASE_URL else RELEASE_BASE_URL
    const val NIN_BASE_URL = "https://api.myidentitypay.com/api/v1/"
    const val TAG = "BluetoothReader"
    const val CONNECTION_CODE = 908
    val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    const val VENDOR_TYPE = 0
    const val BENEFICIARY_TYPE = 1
    //other image size
    const val IMG200 = 200
    const val IMG288 = 288
    const val IMG360 = 360

    //definition of commands
    const val CMD_PASSWORD: Byte = 0x01 //Password

    const val CMD_ENROLID: Byte = 0x02 //Enroll in Device

    const val CMD_VERIFY: Byte = 0x03 //Verify in Device

    const val CMD_IDENTIFY: Byte = 0x04 //Identify in Device

    const val CMD_DELETEID: Byte = 0x05 //Delete in Device

    const val CMD_CLEARID: Byte = 0x06 //Clear in Device


    const val CMD_ENROLHOST: Byte = 0x07 //Enroll to Host

    const val CMD_CAPTUREHOST: Byte = 0x08 //Caputre to Host

    const val CMD_MATCH: Byte = 0x09 //Match

    const val CMD_GETIMAGE: Byte = 0x30 //GETIMAGE

    const val CMD_GETCHAR: Byte = 0x31 //GETDATA


    const val CMD_WRITEFPCARD: Byte = 0x0A //Write Card Data

    const val CMD_READFPCARD: Byte = 0x0B //Read Card Data

    const val CMD_CARDSN: Byte = 0x0E //Read Card Sn

    const val CMD_GETSN: Byte = 0x10

    const val CMD_FPCARDMATCH: Byte = 0x13 //

    const val CMD_WRITEDATACARD: Byte = 0x14 //Write Card Data

    const val CMD_READDATACARD: Byte = 0x15 //Read Card Data

    const val CMD_GETBAT: Byte = 0x21
    const val CMD_UPCARDSN: Byte = 0x43
    const val CMD_GET_VERSION: Byte = 0x22 //Version

    // Message types sent from the BluetoothChatService Handler
    const val MESSAGE_STATE_CHANGE = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5

    // Key names received from the BluetoothChatService Handler
    const val DEVICE_NAME = "device_name"
    const val TOAST = "toast"

    // Intent request codes
    const val REQUEST_ENABLE_BT = 2

    var mBat = ByteArray(2) // data of battery status
    const val REQUEST_PERMISSION_CODE = 1
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)

    const val EXTRA_DEVICE_ADDRESS = "device_address"

    /* Fragment Result Listeners Keys */
    const val FRAGMENT_BENEFICIARY_RESULT_LISTENER: String = "existing_beneficiary_fragment_result_listener"
    const val FRAGMENT_CAMPAIGN_RESULT_LISTENER: String = "campaign_id_fragment_result_listener"
    const val FRAGMENT_NFC_RESULT_LISTENER: String = "nfc_scan_fragment_result_listener"
    const val BENEFICIARY_BUNDLE_KEY: String = "beneficiary_key"
    const val CAMPAIGN_BUNDLE_KEY: String = "campaign_id_key"
    const val NFC_BUNDLE_KEY: String = "nfc_scan_key"

    const val FRAGMENT_LOGIN_RESULT_KEY: String = "login_result_key"
    const val LOGIN_BUNDLE_KEY: String = "login_bundle_key"

    const val API_SUCCESS: String = "success"
    const val API_ERROR: String = "error"

    const val ACTIVE_CASH_FOR_WORK: String = "active"

    const val COMPLETE: String = "Complete"
    const val INCOMPLETE: String = "Incomplete"

    const val SHARED_PREFERENCE_NAME: String = BuildConfig.APPLICATION_ID.plus("_preference")

    const val NIN_KEY: String = "F4Unc5MZ.e7BjG09xd9YJXMnvuXmayuICfMVNW6OE"

    const val TERMS_OF_USE: String = "https://chats.cash/terms-of-use"
    const val PRIVACY_POLICY: String = "https://chats.cash/privacy-policy"
}