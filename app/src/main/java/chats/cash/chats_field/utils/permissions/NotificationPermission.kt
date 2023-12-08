package chats.cash.chats_field.utils.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale

class RequestNotificationPermission(private val activity: Activity) {

    fun onCreate() {
        if (Build.VERSION.SDK_INT > 32) {
            if (activity.isNotificationPermissionGranted()) {
                if (!shouldShowRequestPermissionRationale(activity, NOTIFICATION_PERMISSION)) {
                    activity.getNotificationPermission()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Activity.isNotificationPermissionGranted(): Boolean {
    return (ActivityCompat.checkSelfPermission(this, NOTIFICATION_PERMISSION) == PackageManager.PERMISSION_GRANTED)
}

fun Activity.getNotificationPermission() {
    try {
        if (Build.VERSION.SDK_INT > 32) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(NOTIFICATION_PERMISSION),
                POST_NOTIFICATION_PERMISSION_REQUEST_CODE,
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
const val POST_NOTIFICATION_PERMISSION_REQUEST_CODE = 300
