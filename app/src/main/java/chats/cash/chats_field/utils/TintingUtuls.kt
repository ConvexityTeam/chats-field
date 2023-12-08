package chats.cash.chats_field.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity

fun Activity.setNavigationBarColor(color: Int) {
    if (VersionUtils.hasOreo()) {
        window.navigationBarColor = color
    } else {
        window.navigationBarColor = darkenColor(color)
    }
    setLightNavigationBarAuto(color)
}

fun AppCompatActivity.setNavigationBarColorPreOreo(color: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        window.navigationBarColor = darkenColor(color)
    }
}

@ColorInt
fun darkenColor(@ColorInt color: Int): Int {
    return shiftColor(color, 0.9f)
}

@ColorInt
fun shiftColor(@ColorInt color: Int, @FloatRange(from = 0.0, to = 2.0) by: Float): Int {
    if (by == 1f) return color
    val alpha = Color.alpha(color)
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] *= by // value component
    return (alpha shl 24) + (0x00ffffff and Color.HSVToColor(hsv))
}
///**
// * This will set the color of the view with the id "status_bar" on KitKat and Lollipop. On
// * Lollipop if no such view is found it will set the statusbar color using the native method.
// *
// * @param color the new statusbar color (will be shifted down on Lollipop and above)
// */
//fun AppCompatActivity.setStatusBarColor(color: Int) {
//    val statusBar = window.decorView.rootView.findViewById<View>(R.id.status_bar)
//    if (statusBar != null) {
//        when {
//            VersionUtils.hasMarshmallow() -> statusBar.setBackgroundColor(color)
//            else -> statusBar.setBackgroundColor(
//                ColorUtil.darkenColor(
//                    color
//                )
//            )
//        }
//    } else {
//        when {
//            VersionUtils.hasMarshmallow() -> window.statusBarColor = color
//            else -> window.statusBarColor = ColorUtil.darkenColor(color)
//        }
//    }
//    setLightStatusBarAuto(surfaceColor())
//}

@Suppress("Deprecation")
fun Activity.setLightNavigationBar(enabled: Boolean) {
    if (VersionUtils.hasOreo()) {
        val decorView = window.decorView
        var systemUiVisibility = decorView.systemUiVisibility
        systemUiVisibility = if (enabled) {
            systemUiVisibility or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        decorView.systemUiVisibility = systemUiVisibility
    }
}

fun isColorLight(@ColorInt color: Int): Boolean {
    val darkness =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
    return darkness < 0.4
}

//fun AppCompatActivity.setLightNavigationBarAuto() {
//    setLightNavigationBar(isColorLight())
//}

fun Activity.setLightNavigationBarAuto(bgColor: Int) {
    setLightNavigationBar(isColorLight(bgColor))
}
