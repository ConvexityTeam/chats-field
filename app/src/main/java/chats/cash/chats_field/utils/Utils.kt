package chats.cash.chats_field.utils

import android.content.Context
import com.google.gson.JsonParser
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

object Utils {
    fun Context.checkAppPermission() {
        Permissions.check(this,
            ChatsFieldConstants.permissions,
            null,
            null,
            object : PermissionHandler() {
                override fun onGranted() {

                }

                override fun onBlocked(
                    context: Context?,
                    blockedList: java.util.ArrayList<String>?,
                ): Boolean {
                    return super.onBlocked(context, blockedList)
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String>?,
                ) {
                    super.onDenied(context, deniedPermissions)
                }

                override fun onJustBlocked(
                    context: Context?,
                    justBlockedList: java.util.ArrayList<String>?,
                    deniedPermissions: java.util.ArrayList<String>?,
                ) {
                    super.onJustBlocked(context, justBlockedList, deniedPermissions)
                }
            })

    }

    fun generatePassword(): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(10) {
            chars.random()
        }.joinToString("")
    }

    fun generatePIN(): String {
        val chars = ('0'..'9')
        return List(6) {
            chars.random()
        }.joinToString("")
    }

    fun getErrorMessage(e: HttpException): String {
        val errorJsonString = e.response()?.errorBody()?.string()
        Timber.e("HttpError: $errorJsonString")
        return try {
            JsonParser().parse(errorJsonString)
                .asJsonObject["message"]
                .asString
        } catch (e: Exception) {
            "An error occurred."
        }
    }

    fun String?.toDateTime(): String {
        val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss")
        return DateTime.parse(this).toString(dtf)
    }

    fun String.toCountryCode(): String {
        return "+234${this.drop(1)}"
    }
}