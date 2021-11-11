package com.codose.chats.network.response.tasks


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("tasks")
    val tasks: List<Task>
)