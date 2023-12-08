package chats.cash.chats_field.model

import com.google.gson.annotations.SerializedName

data class ErrorBody(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
)
