package com.codose.chats.views.cashForWork.model

data class AssignedWorker(
    val Assigned_CreatedAt: String,
    val Assigned_Status: String,
    val Assigned_UpdatedAt: String,
    val TaskAssignment: TaskAssignment,
    val dob: String,
    val email: String,
    val first_name: String,
    val gender: String,
    val id: Int,
    val last_name: String,
    val location: String,
    val marital_status: Any,
    val phone: String,
    val profile_pic: String
)