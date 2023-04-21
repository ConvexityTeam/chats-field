package chats.cash.chats_field.network.response

data class RegisterResponse(
    val code: Int,
    val `data`: String,
    val message: String,
    val status: String
)