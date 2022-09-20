package com.codose.chats.views.cashForWork.model

data class TaskDetailsResponse(
    val AssignedWorkers: List<AssignedWorker>,
    val CampaignId: Int,
    val amount: Int,
    val assigned: Int,
    val assignment_count: Int,
    val campleted_task: Int,
    val createdAt: String,
    val description: String,
    val id: Int,
    val isCompleted: Boolean,
    val name: String,
    val require_agent_approval: Boolean,
    val require_evidence: Boolean,
    val require_vendor_approval: Boolean,
    val total_task_allowed: Int,
    val updatedAt: String
)