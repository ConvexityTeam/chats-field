package chats.cash.chats_field.views.cashForWork.model

import com.google.gson.annotations.SerializedName

data class TaskAssignment(
    @SerializedName("TaskId")
    val taskId: Int,
    @SerializedName("UserId")
    val userId: Int,
    val approved: Boolean,
    @SerializedName("approved_at")
    val approvedAt: String?,
    @SerializedName("approved_by")
    val approvedBy: String?,
    @SerializedName("approved_by_agent")
    val approvedByAgent: Boolean,
    @SerializedName("approved_by_vendor")
    val approvedByVendor: Boolean,
    val createdAt: String,
    val id: Int,
    val status: String,
    val updatedAt: String,
    @SerializedName("uploaded_evidence")
    val uploadedEvidence: Boolean,
)
