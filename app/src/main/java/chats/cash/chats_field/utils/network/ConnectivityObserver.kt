package chats.cash.chats_field.utils.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    fun observe(): Flow<NetworkStatus>

    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}
