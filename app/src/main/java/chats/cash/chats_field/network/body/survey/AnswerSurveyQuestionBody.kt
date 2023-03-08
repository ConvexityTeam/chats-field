package chats.cash.chats_field.network.body.survey


import com.google.gson.annotations.SerializedName

data class AnswerSurveyQuestionBody(
    @SerializedName("beneficiaryId")
    val beneficiaryId: Int?,
    @SerializedName("formId")
    val formId: Int?,
    @SerializedName("questions")
    val questions: List<Question?>?
) {
    data class Question(
        @SerializedName("answer")
        val answer: List<String?>?,
        @SerializedName("question")
        val question: String?,
        @SerializedName("reward")
        val reward: Int?,
        @SerializedName("type")
        val type: String?
    )
}