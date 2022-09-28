package chats.cash.chats_field.network.response.campaign

import com.google.gson.annotations.SerializedName

data class CampaignByOrganizationModel(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val cashForWorks: List<CashForWork>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)