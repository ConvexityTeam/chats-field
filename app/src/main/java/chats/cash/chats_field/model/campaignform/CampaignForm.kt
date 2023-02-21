package chats.cash.chats_field.model.campaignform


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import chats.cash.chats_field.model.ModelCampaign
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "allCampaignForm")

@Parcelize
data class CampaignForm(
    @SerializedName("beneficiaryId")
    val beneficiaryId: Int?,
    @SerializedName("campaigns")
    val campaigns: List<ModelCampaign>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("id")
    @PrimaryKey
    val id: Int,
    @SerializedName("organisationId")
    val organisationId: Int,
    @SerializedName("questions")
    val questions: List<CampaignQuestion>,
    @SerializedName("title")
    val title: String,
    @SerializedName("updatedAt")
    val updatedAt: String
):Parcelable