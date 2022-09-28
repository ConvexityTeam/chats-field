package chats.cash.chats_field.network.response.organization.campaign

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "campaign")
data class Campaign(
    @SerializedName("budget")
    val budget: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("id")
    @PrimaryKey
    val id: Int,
    @SerializedName("location")
    val location: String,
    @SerializedName("OrganisationMemberId")
    val organisationMemberId: Int,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("updatedAt")
    val updatedAt: String
){
    override fun toString(): String {
        return title
    }
}