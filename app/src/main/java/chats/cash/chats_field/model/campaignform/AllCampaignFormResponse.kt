package chats.cash.chats_field.model.campaignform


import com.google.gson.annotations.SerializedName


data class AllCampaignFormResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: List<CampaignForm>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)