package com.codose.chats.network.response


import com.google.gson.annotations.SerializedName

data class NfcUpdateResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)