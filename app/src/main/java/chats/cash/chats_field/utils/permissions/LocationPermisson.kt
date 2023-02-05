package chats.cash.chats_field.utils.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity

class LocationPermisson(onPermissionGranted:() -> Unit = {}, onPermissionDenied:() -> Unit ={},
                        val context: FragmentActivity
) {

    fun locationPermissionRequest(callback: (Boolean) -> Unit) = context.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isLocationPermissionsGranted(callback)
    }



    fun isLocationPermissionsGranted(callback:(Boolean) -> Unit){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val shouldShowRequestRationale = context.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            if(shouldShowRequestRationale){
                callback(false)
                return
            }
            locationPermissionRequest(callback).launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))

        }
        else {
            callback(true)
        }

    }

}