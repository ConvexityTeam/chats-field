package com.codose.chats.network.response

data class UserDetailsResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: String
)