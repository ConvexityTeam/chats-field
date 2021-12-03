package com.codose.chats.network.response.campaign

import com.codose.chats.model.ModelCampaign
import com.google.gson.annotations.SerializedName

data class GetAllCampaignsResponse(
    @SerializedName("code")
    var code: Int,
    @SerializedName("status")
    var status: String?,
    @SerializedName("message")
    var message: String?,
    @SerializedName("data")
    var data: List<ModelCampaign>
)
