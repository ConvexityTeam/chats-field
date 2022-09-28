package chats.cash.chats_field.network.response.tasks.details

import com.google.gson.annotations.SerializedName

data class AssociatedWorker(
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("Worker")
    val worker: Worker
)