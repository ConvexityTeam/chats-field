package com.codose.chats.network.response.tasks.details


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("task")
    val task: Task
)