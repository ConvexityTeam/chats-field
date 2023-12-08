package chats.cash.chats_field.network.response.organization.campaign

import com.google.gson.annotations.SerializedName

data class CampaignResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: List<Campaign>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
)
