package chats.cash.chats_field.network.body.impact_report

import com.google.gson.annotations.SerializedName

data class ImpactReportBody(
    @SerializedName("AgentId")
    val agentId: String,
    @SerializedName("CampaignId")
    val campaignId: Int,
    @SerializedName("MediaLink")
    val mediaLink: String,
    @SerializedName("title")
    val title: String,
)
