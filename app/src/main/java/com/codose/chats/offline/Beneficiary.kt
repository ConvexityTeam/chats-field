package com.codose.chats.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.codose.chats.utils.BluetoothConstants.VENDOR_TYPE

@Entity(tableName = "beneficiary")
data class Beneficiary(
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
    var firstName : String = "",
    var lastName :String = "",
    var email : String = "",
    var phone : String = "",
    var longitude : Double = 0.0,
    var latitude : Double = 0.0,
    var password : String = "",
    var status : Int = 0,
    var nfc : String = "",
    var bvn : String = "",
    var rightThumb : String = "",
    var rightLittle : String = "",
    var rightIndex : String = "",
    var leftThumb : String = "",
    var leftLittle : String= "",
    var leftIndex : String = "",
    var profilePic : String = "",
    var storeName : String = "",
    var gender : String = "",
    var date : String = "",
    var campaignId :String = "",
    var pin : Int = 0,
    var nin : String = "",
    var isSpecialCase : Boolean = false,
    var type : Int = VENDOR_TYPE
)
