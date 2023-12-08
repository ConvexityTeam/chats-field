package chats.cash.chats_field.network.api

import chats.cash.chats_field.utils.PreferenceUtilInterface
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(private val preferenceUtil: PreferenceUtilInterface) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)

        // Check if the response is a 401 Unauthorized error

        // Check if the response is a 401 Unauthorized error
        if (response.code == 401) {
            preferenceUtil.clearPreference()
        }

        // If the response is not a 401 error, simply return the response

        // If the response is not a 401 error, simply return the response
        return response
    }
}
