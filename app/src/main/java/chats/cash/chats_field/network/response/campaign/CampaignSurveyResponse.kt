package chats.cash.chats_field.network.response.campaign


import com.google.gson.annotations.SerializedName

data class CampaignSurveyResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val campaignSurveyResponseData: CampaignSurveyResponseData,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
) {
    data class CampaignSurveyResponseData(
        @SerializedName("beneficiaryId")
        val beneficiaryId: Int,
        @SerializedName("campaignId")
        val campaignId: Int,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("formId")
        val formId: Int,
        @SerializedName("id")
        val id: Int,
        @SerializedName("questions")
        val questions: List<Question>,
        @SerializedName("updatedAt")
        val updatedAt: String
    ) {
        data class Question(
            @SerializedName("answer")
            val answer: String,
            @SerializedName("question")
            val question: String,
            @SerializedName("reward")
            val reward: Int,
            @SerializedName("type")
            val type: String
        )
    }
}