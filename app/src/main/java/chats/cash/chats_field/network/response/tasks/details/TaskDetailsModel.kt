package chats.cash.chats_field.network.response.tasks.details

import com.google.gson.annotations.SerializedName

data class TaskDetailsModel(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)