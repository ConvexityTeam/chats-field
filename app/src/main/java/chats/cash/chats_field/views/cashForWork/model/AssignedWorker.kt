package chats.cash.chats_field.views.cashForWork.model

import com.google.gson.annotations.SerializedName

data class AssignedWorker(
    @SerializedName("Assigned_CreatedAt")
    val assignedCreatedAt: String,
    @SerializedName("Assigned_Status")
    val assignedStatus: String,
    @SerializedName("Assigned_UpdatedAt")
    val assignedUpdatedAt: String,
    @SerializedName("TaskAssignment")
    val taskAssignment: TaskAssignment,
    val dob: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    val gender: String,
    val id: Int,
    @SerializedName("last_name")
    val lastName: String,
    val location: String,
    @SerializedName("marital_status")
    val maritalStatus: Any,
    val phone: String,
    @SerializedName("profile_pic")
    val profilePic: String,
)
