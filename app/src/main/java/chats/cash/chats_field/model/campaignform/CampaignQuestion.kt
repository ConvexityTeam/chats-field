package chats.cash.chats_field.model.campaignform


import android.os.Parcelable
import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CampaignQuestion(
    @SerializedName("question")
    @Embedded(prefix = "questionX")
    val question: QuestionX,
    @SerializedName("required")
    val required: Boolean,
    @SerializedName("type")
    val type: String,
    @SerializedName("value")
    val value: String
):Parcelable