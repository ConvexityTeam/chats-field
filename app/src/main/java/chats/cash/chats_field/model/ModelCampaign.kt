package chats.cash.chats_field.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import chats.cash.chats_field.views.cashForWork.model.Job
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity(tableName = "modelCampaign")
@Parcelize
data class ModelCampaign(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("OrganisationId")
    val OrganisationId: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("spending")
    val spending: String?,
    @SerializedName("Beneficiaries")
    var Beneficiaries: List<CampaignBeneficiaries>? = emptyList(),
    @SerializedName("description")
    val description: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("is_funded")
    val is_funded: String?,
    @SerializedName("funded_with")
    val funded_with: String?,
    @SerializedName("budget")
    val budget: String?,
    @Ignore
    @SerializedName("location")
    val location: Location?,
    @SerializedName("start_date")
    val start_date: String?,
    @SerializedName("end_date")
    val end_date: String?,
    @SerializedName("ck8")
    val ck8: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("Jobs")
    @Ignore
    val jobs: List<Job>,
) : Parcelable {
    @Parcelize
    data class Location(
        val country: String,
        val state: List<String>,
    ) : Parcelable

    @Parcelize
    data class CampaignBeneficiaries(
        val id: Int,
        val email: String,
        val phone: String,
        val first_name: String,
        val last_name: String,
    ) : Parcelable

    constructor(
        id: Int,
        OrganisationId: Int,
        title: String?,
        type: String?,
        spending: String?,
        description: String?,
        status: String?,
        is_funded: String?,
        funded_with: String?,
        budget: String?,
        start_date: String?,
        end_date: String?,
        ck8: String?,
        createdAt: String?,
        updatedAt: String,
    ) : this(
        id,
        OrganisationId,
        title,
        type,
        spending,
        null,
        description,
        status,
        is_funded,
        funded_with,
        budget,
        null,
        start_date,
        end_date,
        ck8,
        createdAt,
        updatedAt,
        emptyList(),
    )
}
