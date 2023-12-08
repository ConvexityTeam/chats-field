package chats.cash.chats_field.network.body.survey

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = SURVEY_ANSWERS_TABLE_NAME)
data class SubmitSurveyAnswerBody(
    @PrimaryKey
    val email: String,
    val campaignId: Int,
    @SerializedName("beneficiaryId")
    val beneficiaryId: Int,
    @SerializedName("formId")
    val formId: Int,
    @SerializedName("questions")
    val questionsAnswers: List<QuestionAnswers>,
) {
    data class QuestionAnswers(
        @SerializedName("answer")
        val answer: List<String>,
        @SerializedName("question")
        val question: String,
        @SerializedName("reward")
        val reward: List<Int>,
        @SerializedName("type")
        val type: String,
    )
}

const val SURVEY_ANSWERS_TABLE_NAME = "survey_answers"
