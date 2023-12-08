package chats.cash.chats_field.model.campaignform

import com.google.gson.annotations.SerializedName

data class AllCampaignFormResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: CampaignFormData,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
) {
    data class CampaignFormData(
        @SerializedName("totalItems")
        val totalItems: Int,
        @SerializedName("data")
        val data: List<CampaignForm>,
    )
}
