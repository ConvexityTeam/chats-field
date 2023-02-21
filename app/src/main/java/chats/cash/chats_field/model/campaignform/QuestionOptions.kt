package chats.cash.chats_field.model.campaignform


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuestionOptions(
    @SerializedName("option")
    val option: String,
    @SerializedName("reward")
    val reward: String
):Parcelable