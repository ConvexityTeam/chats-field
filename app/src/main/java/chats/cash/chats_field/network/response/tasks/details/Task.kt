package chats.cash.chats_field.network.response.tasks.details

import com.google.gson.annotations.SerializedName

data class Task(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("AssociatedWorkers")
    val associatedWorkers: List<AssociatedWorker>,
    @SerializedName("Campaign")
    val campaign: Campaign,
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
    val updatedAt: String,
)
