package com.codose.chats.network.response.progress


import com.google.gson.annotations.SerializedName

data class SubmitProgressModel(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)