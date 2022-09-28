package chats.cash.chats_field.network.response.tasks.details

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("task")
    val task: Task
)