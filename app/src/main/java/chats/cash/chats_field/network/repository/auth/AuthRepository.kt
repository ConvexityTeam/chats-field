package chats.cash.chats_field.network.repository.auth

import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.interfaces.ConvexityDataSourceInterface
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.network.response.forgot.ForgotPasswordResponse
import chats.cash.chats_field.network.response.login.LoginResponse
import chats.cash.chats_field.network.response.login.User
import chats.cash.chats_field.utils.PreferenceUtilInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AuthRepository(
    private val dataSource: ConvexityDataSourceInterface,
    private val preferenceUtil: PreferenceUtilInterface,
) : AuthRepositoryInterface {
    override suspend fun logout() {
        preferenceUtil.clearPreference()
    }

    override suspend fun loginNGO(loginBody: LoginBody): Flow<NetworkResponse<LoginResponse>> {
        return dataSource.loginNGO(loginBody) { data ->
            preferenceUtil.setNGOToken("Bearer " + data.data.token)
            preferenceUtil.saveProfile(data.data.user)
            data.data.user.associatedOrganisations.firstOrNull()?.let { org ->
                preferenceUtil.setNGO(org.OrganisationId, org.Organisation.name ?: "Unknown")
            }
            CoroutineScope(Dispatchers.IO).launch {
                data.data.user.associatedOrganisations.firstOrNull()?.OrganisationId?.let { id ->
                    async {
                        dataSource.getAllCampaigns(id, data.data.token)
                    }
                    async {
                        dataSource.getAllCampaignForms(id, data.data.token)
                    }
                }
            }
        }
    }

    override suspend fun sendForgotEmail(email: String): Flow<NetworkResponse<ForgotPasswordResponse>> {
        return dataSource.sendForgotEmail(email)
    }

    override fun getUserProfile(): User? {
        return preferenceUtil.getProfile()
    }
}
