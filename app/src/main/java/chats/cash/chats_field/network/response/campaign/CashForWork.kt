package chats.cash.chats_field.network.response.campaign

import com.google.gson.annotations.SerializedName

data class CashForWork(
    @SerializedName("beneficiaries_count")
    val beneficiariesCount: Int,
    @SerializedName("budget")
    val budget: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("location")
    val location: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)