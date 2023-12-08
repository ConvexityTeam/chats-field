package chats.cash.chats_field.utils

sealed class ApiResponse<out T> {
    class Loading<out T> : ApiResponse<T>()
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Failure<out T>(val message: String, val code: Int = 0) : ApiResponse<T>()
}
