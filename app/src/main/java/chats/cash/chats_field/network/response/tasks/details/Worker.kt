package chats.cash.chats_field.network.response.tasks.details

import com.google.gson.annotations.SerializedName

data class Worker(
    @SerializedName("address")
    val address: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("marital_status")
    val maritalStatus: Any,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("referal_id")
    val referalId: Any,
    @SerializedName("RoleId")
    val roleId: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)