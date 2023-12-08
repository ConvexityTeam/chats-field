package chats.cash.chats_field.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import chats.cash.chats_field.utils.network.tedt.isOnline
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

@Suppress("DEPRECATION")
class ConnectivityInterceptor(context: Context) : Interceptor {

    val appContext: Context? = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        appContext?.apply {
            if (!isOnline(appContext)) {
                throw NoConnectivityException()
            }
        }
        return chain.proceed(chain.request())
    }
}
object tedt {
    fun isOnline(appContext: Context): Boolean {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun networkInfo(): Boolean {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            if (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            ) {
                return true
            }

            return false
        }
        return networkInfo()
    }
}

class NoConnectivityException : IOException()
