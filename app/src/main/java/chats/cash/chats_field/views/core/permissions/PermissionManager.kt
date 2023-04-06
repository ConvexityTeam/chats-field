package chats.cash.chats_field.views.core.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionManager(private val context: AppCompatActivity, private val permissionResultReceiver: PermissionResultReceiver) {

    var currrentPermission:String? = null

    private val requestPermissionLauncher =
        context.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                permissionResultReceiver.onGranted()
            } else {
                currrentPermission?.let {
                    if (context.shouldShowRequestPermissionRationale(it)){
                        permissionResultReceiver.showRationale(it)
                    }
                    else{
                        permissionResultReceiver.onDenied(it)
                    }
                }
            }
            currrentPermission = null
        }

    private val requestPermissionLauncher2 =
        context.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.keys.forEach {key ->
                val isGranted = permissions[key]?:false
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    permissionResultReceiver.onGranted()
                } else {
                    key.let {
                        if (context.shouldShowRequestPermissionRationale(it)) {
                            permissionResultReceiver.showRationale(it)
                        } else {
                            permissionResultReceiver.onDenied(it)
                        }
                    }
                }
                currrentPermission = null
            }
        }



    fun getPermission(permission:String){
        currrentPermission = permission
        requestPermissionLauncher.launch(permission)
    }


    fun checkPermissions( permissions:List<String>){
        permissions.forEach {permission ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionResultReceiver.onGranted()
                // You can use the API that requires the permission.
            } else {
                    permissionResultReceiver.notGranted(permission)
                }
            }
        }


    fun shouldShowRationale(permission:String):Boolean{
        return context.shouldShowRequestPermissionRationale(permission)
    }

    fun getPermissions(listOf: List<String>) {
        requestPermissionLauncher2.launch(listOf.toTypedArray())
    }
}


interface PermissionResultReceiver{
    fun onGranted()
    fun notGranted(permission: String)

    fun onDenied(permission:String)

    fun showRationale(permission:String)
}

const val CAMERA_PERMISSION = Manifest.permission.CAMERA
const val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
const val WRITE_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION

fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}

fun Context.checkPermission(permission:String):Boolean{
    return (ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED)

}