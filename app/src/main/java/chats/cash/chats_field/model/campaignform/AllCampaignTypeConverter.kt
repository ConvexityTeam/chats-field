package chats.cash.chats_field.model.campaignform

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.utils.JsonParser
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import com.google.gson.reflect.TypeToken


@ProvidedTypeConverter
class AllCampaignTypeConverter(private val jsonParser: JsonParser) {

    @TypeConverter
    fun fromModelCampaignJson(json: String): List<ModelCampaign> {
        return jsonParser.fromJson<ArrayList<ModelCampaign>>(
            json,
            object : TypeToken<ArrayList<ModelCampaign>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toModelCampaignJson(events: List<ModelCampaign>): String {
        return jsonParser.toJson(events, object : TypeToken<ArrayList<ModelCampaign>>() {}.type)
            ?: "[]"
    }

    @TypeConverter
    fun fromQuestionsJson(json: String): List<CampaignQuestion> {
        return jsonParser.fromJson<ArrayList<CampaignQuestion>>(
            json,
            object : TypeToken<ArrayList<CampaignQuestion>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toQuestionsJson(events: List<CampaignQuestion>): String {
        return jsonParser.toJson(events, object : TypeToken<ArrayList<CampaignQuestion>>() {}.type)
            ?: "[]"
    }

    @TypeConverter
    fun fromQuestionsOptionJson(json: String): List<QuestionOptions> {
        return jsonParser.fromJson<ArrayList<QuestionOptions>>(
            json,
            object : TypeToken<ArrayList<QuestionOptions>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toQuestionsOptionsJson(events: List<QuestionOptions>): String {
        return jsonParser.toJson(events, object : TypeToken<ArrayList<QuestionOptions>>() {}.type)
            ?: "[]"
    }

    @TypeConverter
    fun fromQuestionsAnswersJson(json: String): List<SubmitSurveyAnswerBody.QuestionAnswers> {
        return jsonParser.fromJson<ArrayList<SubmitSurveyAnswerBody.QuestionAnswers>>(json,
            object : TypeToken<ArrayList<SubmitSurveyAnswerBody.QuestionAnswers>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toQuestionsAnswersJson(events: List<SubmitSurveyAnswerBody.QuestionAnswers>): String {
        return jsonParser.toJson(events,
            object : TypeToken<ArrayList<SubmitSurveyAnswerBody.QuestionAnswers>>() {}.type) ?: "[]"
    }


}