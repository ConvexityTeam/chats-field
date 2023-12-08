package chats.cash.chats_field.network.response.beneficiary_onboarding

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "organization_beneficiary")
data class OrganizationBeneficiary(
    val dob: String?,
    val email: String?,
    @SerializedName("first_name")
    val firstName: String?,
    val gender: String?,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("marital_status")
    val maritalStatus: String?,
    val phone: String?,
    @SerializedName("profile_pic")
    val profilePic: String?,
    val iris: String?,
)
