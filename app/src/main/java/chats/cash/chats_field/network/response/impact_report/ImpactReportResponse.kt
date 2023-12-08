package chats.cash.chats_field.network.response.impact_report

import com.google.gson.annotations.SerializedName

data class ImpactReportResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
) {
    data class Data(
        @SerializedName("AgentId")
        val agentId: Int,
        @SerializedName("CampaignId")
        val campaignId: Int,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("MediaLink")
        val mediaLink: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
    )
}
