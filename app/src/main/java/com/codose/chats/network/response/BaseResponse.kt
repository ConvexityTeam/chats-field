package com.codose.chats.network.response

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: T,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)
