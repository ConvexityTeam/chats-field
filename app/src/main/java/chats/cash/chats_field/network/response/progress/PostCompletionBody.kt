package chats.cash.chats_field.network.response.progress

import com.google.gson.annotations.SerializedName

data class PostCompletionBody(
    @SerializedName("taskId")
    val taskId: Int,
    @SerializedName("userId")
    val userId: Int,
)
