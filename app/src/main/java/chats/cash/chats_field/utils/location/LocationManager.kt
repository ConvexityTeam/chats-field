package chats.cash.chats_field.utils.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import chats.cash.chats_field.views.core.permissions.COARSE_LOCATION
import chats.cash.chats_field.views.core.permissions.FINE_LOCATION
import chats.cash.chats_field.views.core.permissions.checkPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class LocationManager(val context: FragmentActivity) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocationAsync(): Deferred<UserLocation?> {
        val lastKnownLocation = CompletableDeferred<UserLocation?>()
        if (context.checkPermission(COARSE_LOCATION) && context.checkPermission(FINE_LOCATION)) {
            val currentLocationRequests = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null,
            )
            currentLocationRequests.addOnSuccessListener {
                it?.let {
                    lastKnownLocation.complete(UserLocation(it.longitude, it.latitude))
                }
            }.addOnFailureListener {
                val request = fusedLocationClient.lastLocation
                request.addOnSuccessListener {
                    it?.let { location ->
                        lastKnownLocation.complete(
                            UserLocation(
                                location.longitude,
                                location.latitude,
                            ),
                        )
                    }
                }.addOnFailureListener { lastKnownLocation.complete(null) }
            }
        }

        return lastKnownLocation
    }
}

data class UserLocation(val longitude: Double, val latitude: Double)
fun Context.openLocationSetting() = startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
