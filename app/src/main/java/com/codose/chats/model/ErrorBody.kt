package com.codose.chats.model


import com.google.gson.annotations.SerializedName

data class ErrorBody(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)