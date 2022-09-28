package chats.cash.chats_field.views.cashForWork.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job(
    @SerializedName("CampaignId")
    val campaignId: Int,
    val amount: Int,
    val assigned: Int,
    val assignment_count: Int,
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
    val updatedAt: String
): Parcelable
