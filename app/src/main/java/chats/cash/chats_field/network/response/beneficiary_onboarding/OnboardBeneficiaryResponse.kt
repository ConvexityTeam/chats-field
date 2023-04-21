package chats.cash.chats_field.network.response.beneficiary_onboarding


import com.google.gson.annotations.SerializedName

data class OnboardBeneficiaryResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val beneficiaryID: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)