package chats.cash.chats_field.network.body

import com.google.gson.annotations.SerializedName

data class LocationBody(
    @SerializedName("coordinates")
    val coordinates: List<Double>,
    @SerializedName("country")
    val country: String
)