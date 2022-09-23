package com.codose.chats.di.nin_model

data class NinBaseResponse(
    val detail: String,
    val nin_data: NinData,
    val response_code: String,
    val status: Boolean
)