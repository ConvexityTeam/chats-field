package chats.cash.chats_field.network.response

data class RegisterResponse(
    val code: Int,
    val `data`: Int,
    val message: String,
    val status: String
)