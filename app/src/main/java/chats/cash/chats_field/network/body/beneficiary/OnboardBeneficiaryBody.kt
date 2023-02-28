package chats.cash.chats_field.network.body.beneficiary

import java.io.File

data class OnboardBeneficiaryBody(
    val firstName: String,
    val lastName: String,
    val email:String,
    val phone:String,
    val password:String,
    val latitude:String,
    val longitude:String,
    val nfc:String,
    val role:String,
    val profile_pic:String,
    val prints: File,
    val gender:String,
    val dob:String,
    val campaign:String,
    val pin:String
)


data class OnboardSpecialBeneficiaryBody(
    val firstName: String,
    val lastName: String,
    val email:String,
    val phone:String,
    val password:String,
    val latitude:String,
    val longitude:String,
    val nfc:String,
    val role:String,
    val profile_pic:String,
    val gender:String,
    val dob:String,
    val campaign:String,
    val pin:String
)