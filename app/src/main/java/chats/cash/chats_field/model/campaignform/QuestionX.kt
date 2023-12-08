package chats.cash.chats_field.model.campaignform

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionX(
    @SerializedName("options")
    val options: List<QuestionOptions>,
    @SerializedName("title")
    val title: String,
) : Parcelable
