package com.codose.chats.views.cashForWork.model

data class TaskAssignment(
    val TaskId: Int,
    val UserId: Int,
    val approved: Boolean,
    val approved_at: Any,
    val approved_by: Any,
    val approved_by_agent: Boolean,
    val approved_by_vendor: Boolean,
    val createdAt: String,
    val id: Int,
    val status: String,
    val updatedAt: String,
    val uploaded_evidence: Boolean
)