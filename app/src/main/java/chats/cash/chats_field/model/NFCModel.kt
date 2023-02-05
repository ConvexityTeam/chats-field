package chats.cash.chats_field.model

import com.google.gson.annotations.SerializedName

data class NFCModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nfc")
    val nfc: String
)