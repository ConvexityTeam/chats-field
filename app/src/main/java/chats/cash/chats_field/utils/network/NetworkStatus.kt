package chats.cash.chats_field.utils.network

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
    object Losing : NetworkStatus()
    object Lost : NetworkStatus()
}
