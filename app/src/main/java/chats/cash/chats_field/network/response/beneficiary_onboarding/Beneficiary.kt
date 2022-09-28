package chats.cash.chats_field.network.response.beneficiary_onboarding

import com.google.gson.annotations.SerializedName

data class Beneficiary(
    val dob: String?,
    val email: String?,
    @SerializedName("first_name")
    val firstName: String?,
    val gender: String?,
    val id: Int,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("marital_status")
    val maritalStatus: String?,
    val phone: String?,
    @SerializedName("profile_pic")
    val profilePic: String?
)
