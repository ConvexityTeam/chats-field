package chats.cash.chats_field.network.repository.auth

import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.login.User
import kotlinx.coroutines.flow.Flow

interface AuthRepositoryInterface {

    suspend fun logout()

    suspend fun loginNGO(loginBody: LoginBody):
        Flow<NetworkResponse<LoginResponse>>

    /**
     * resets password of an ngo account by sending email
     * @param email email of the account to send password reset to
     * @return public final data class ForgotPasswordResponse(
     *     val message: String,
     *     val status: String
     * )
     *   [chats.cash.chats_field.network.response.forgot]
     */
    suspend fun sendForgotEmail(email: String): Flow<NetworkResponse<ForgotPasswordResponse>>

    /**
     * returns details of a field agent like name, profile picture and etc.
     *
     */

    fun getUserProfile(): User?
}
