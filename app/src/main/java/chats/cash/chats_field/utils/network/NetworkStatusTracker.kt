package chats.cash.chats_field.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkStatusTracker(private val context: Context) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<NetworkStatus> {
        return callbackFlow {
            val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onUnavailable() {
                    Log.d("SNETWORK", "UNavailable")
                    super.onUnavailable()
                    launch { send(NetworkStatus.Unavailable) }
                }

                override fun onAvailable(network: Network) {
                    Log.d("SNETWORK", "available")
                    super.onAvailable(network)
                    launch { send(NetworkStatus.Available) }
                }

                override fun onLost(network: Network) {
                    Log.d("SNETWORK", "onlost")
                    super.onLost(network)
                    launch { send(NetworkStatus.Lost) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    Log.d("SNETWORK", "losing")
                    super.onLosing(network, maxMsToLive)
                    launch { send(NetworkStatus.Losing) }
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkStatusCallback)
            } else {
                connectivityManager.registerNetworkCallback(request, networkStatusCallback)
            }

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkStatusCallback)
            }
        }.distinctUntilChanged()
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }
}
