package chats.cash.chats_field.views.cashForWork.model

import com.google.gson.annotations.SerializedName

data class TaskDetailsResponse(
    @SerializedName("AssignedWorkers")
    val assignedWorkers: List<AssignedWorker>,
    @SerializedName("CampaignId")
    val campaignId: Int,
    val amount: Int,
    val assigned: Int,
    @SerializedName("assignment_count")
    val assignmentCount: Int,
    @SerializedName("campleted_task")
    val completedTask: Int,
    val createdAt: String,
    val description: String,
    val id: Int,
    val isCompleted: Boolean,
    val name: String,
    @SerializedName("require_agent_approval")
    val requireAgentApproval: Boolean,
    @SerializedName("require_evidence")
    val requireEvidence: Boolean,
    @SerializedName("require_vendor_approval")
    val requireVendorApproval: Boolean,
    @SerializedName("total_task_allowed")
    val totalTaskAllowed: Int,
    val updatedAt: String
)
