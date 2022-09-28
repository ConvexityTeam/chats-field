package chats.cash.chats_field.network.response

data class UserDetailsResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: String
)