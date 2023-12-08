package chats.cash.chats_field.network.repository

import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.utils.handleThrowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun <T> Flow<NetworkResponse<T>>.defaultHandler(
    errorMessage: String,
    socketError: String = "",
    networkError: String = "",
): Flow<NetworkResponse<T>> {
    return catch {
        if (it is UnknownHostException || it is SocketTimeoutException) {
            emit(NetworkResponse.NetworkError())
        } else {
            it.printStackTrace()
            emit(
                NetworkResponse.Error(
                    it.handleThrowable(
                        errorMessage,
                        socketError,
                        networkError,
                    ),
                    it,
                ),
            )
        }
    }.onStart {
        emit(NetworkResponse.Loading())
    }
}
