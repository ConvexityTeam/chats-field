package chats.cash.chats_field.network.api

import chats.cash.chats_field.utils.ChatsFieldConstants.NIN_KEY
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NinVerificationApi {

    @POST("biometrics/merchant/data/verification/nin_wo_face")
    suspend fun verifyNin(
        @Body nonBody: NinBody,
        @Header("x-api-key") apiKey: String = NIN_KEY
    ): NinResponse

    data class NinBody(val number: String?)

    data class NinResponse(
        val status: Boolean,
        val message: String,
        val responseCode: String,
        val ninData: NinData?,
    )

    data class NinData(
        val nin: String,
        val firstname: String,
        val surname: String
    )
}
