package com.codose.chats.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "modelCampaign")
data class ModelCampaign (

    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("OrganisationId")
    val OrganisationId: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("spending")
    val spending: String?,
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
    @SerializedName("location")
    val location: String?,
    @SerializedName("start_date")
    val start_date: String?,
    @SerializedName("end_date")
    val end_date: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String
)