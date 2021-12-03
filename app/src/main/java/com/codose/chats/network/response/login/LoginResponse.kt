package com.codose.chats.network.response.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)