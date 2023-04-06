package chats.cash.chats_field.utils.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.provider.Settings
import chats.cash.chats_field.utils.dialogs.AlertDialog
import chats.cash.chats_field.views.core.permissions.openAppSystemSettings


class LocationBroadCastListener  : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val locationManager:android.location.LocationManager = (context.getSystemService(LOCATION_SERVICE) as android.location.LocationManager)
            if(!locationManager.isLocationEnabled){
                AlertDialog(context,"Location not enabled",
                    "Your location has been turned off, please switch it on",
                    onNegativeClicked = {

                    }, onPostiveClicked = {
                        context.openLocationSetting()
                    }).show()
            }
            else if (!locationManager.isProviderEnabled(android.location.LocationManager.FUSED_PROVIDER)) {

                AlertDialog(context,"Location not enabled",
                    "Your location has been turned off, please switch it on",
                    onNegativeClicked = {

                    }, onPostiveClicked = {

                    }).show()

            }else if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {

                AlertDialog(context,"Location not enabled",
                    "Your location has been turned off, please switch it on",
                    onNegativeClicked = {

                    }, onPostiveClicked = {

                    }).show()

            }
        } catch (ex: Exception) {

        }
    }
}