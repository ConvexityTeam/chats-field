package com.codose.chats.network.body

data class VendorBody(
    val bvn: String,
    val email: String,
    val name: String,
    val password: String,
    val phone: String,
    val pin: String,
    val store_name: String,
    val first_name: String,
    val last_name: String,
)