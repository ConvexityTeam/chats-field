package chats.cash.chats_field.utils.location

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import chats.cash.chats_field.utils.permissions.LocationPermisson
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*

class LocationManager(val context: FragmentActivity) {
   private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

   @SuppressLint("MissingPermission")
   suspend fun getLastKnownLocationAsync():Deferred<UserLocation?>  {
       val lastKnownLocation = CompletableDeferred<UserLocation?>()
       LocationPermisson(context = context).isLocationPermissionsGranted {
           if(it){

               val currentLocationRequests =  fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
               currentLocationRequests.addOnSuccessListener {
                   it?.let {
                       lastKnownLocation.complete(UserLocation(it.longitude, it.latitude))
                   }
               }.addOnFailureListener { lastKnownLocation.complete(null) }

               val request = fusedLocationClient.lastLocation
               request .addOnSuccessListener {
                   it?.let {location->
                       lastKnownLocation.complete(UserLocation(location.longitude, location.latitude))
                   }
               }.addOnFailureListener { lastKnownLocation.complete(null) }

           }
       }
       return lastKnownLocation

   }


}

data class UserLocation(val longitude:Double, val latitude:Double)