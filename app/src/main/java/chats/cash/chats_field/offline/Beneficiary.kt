package chats.cash.chats_field.offline

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import chats.cash.chats_field.utils.ChatsFieldConstants.VENDOR_TYPE
import com.google.gson.annotations.SerializedName

@Entity(tableName = "beneficiary", primaryKeys = ["firstName", "lastName"])
data class Beneficiary(
    var id: Int? = null,
    @SerializedName("first_name")
    var firstName: String = "",
    @SerializedName("last_name")
    var lastName: String = "",
    var email: String = "",
    var phone: String = "",
    var address: String = "",
    var state: String = "",
    var country: String = "",
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var password: String = "",
    var status: Int ? = null,
    var nfc: String = "",
    var bvn: String = "",
    var rightThumb: String = "",
    var rightLittle: String = "",
    var rightIndex: String = "",
    var leftThumb: String = "",
    var leftLittle: String = "",
    var leftIndex: String = "",
    @SerializedName("profile_pic")
    var profilePic: String = "",
    var storeName: String = "",
    var gender: String = "",
    @SerializedName("dob")
    var date: String = "",
    @SerializedName("campaign")
    var campaignId: String = "",
    var pin: String = "",
    @Ignore()
    var location: String? = null,
    var nin: String = "",
    var iris: String? = "",
    var isSpecialCase: Boolean = false,
    var type: Int = VENDOR_TYPE,
    var isGroup: Boolean = false,
    @Ignore
    var allFingers: ArrayList<Bitmap>? = null,
)
