package com.codose.chats.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.codose.chats.offline.Beneficiary
import java.util.*

object BluetoothConstants {
    const val sDirectory = ""
    const val BASE_URL = "https://api.chats.cash/v1/"
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

}