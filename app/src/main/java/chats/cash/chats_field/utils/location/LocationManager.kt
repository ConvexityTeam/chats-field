package chats.cash.chats_field.utils.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import chats.cash.chats_field.utils.permissions.LocationPermisson
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

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
                   lastKnownLocation.complete(UserLocation(it.longitude, it.latitude))
               }.addOnFailureListener { lastKnownLocation.complete(null) }

           }
       }
       return lastKnownLocation

   }


}

data class UserLocation(val long:Double, val latitude:Double)