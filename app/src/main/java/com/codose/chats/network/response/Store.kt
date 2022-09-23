package com.codose.chats.network.response

data class Store(
    val UserId: Int,
    val address: String,
    val createdAt: String,
    val id: Int,
    val location: String,
    val store_name: String,
    val updatedAt: String
)