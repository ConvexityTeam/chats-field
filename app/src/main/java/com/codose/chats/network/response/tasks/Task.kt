package com.codose.chats.network.response.tasks


import com.google.gson.annotations.SerializedName

data class Task(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)