package chats.cash.chats_field.network.api.imageServices

import kotlinx.coroutines.Deferred

interface ImageServiceProvider {

    suspend fun uploadImage(
        path: String,
        fileName: String,
        shouldCompressImage: Boolean = false,
        onProgress: (Float) -> Unit,
    ): Deferred<String?>
}
