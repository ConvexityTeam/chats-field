package chats.cash.chats_field.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import chats.cash.chats_field.network.response.login.User
import chats.cash.chats_field.utils.ChatsFieldConstants.SHARED_PREFERENCE_NAME
import com.google.gson.Gson

class PreferenceUtil(
    private val context: Context,
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        SHARED_PREFERENCE_NAME,
        Context.MODE_PRIVATE,
    ),
) : PreferenceUtilInterface {

    override
    fun setNGO(id: Int, name: String) {
        sharedPreferences.edit(commit = true) {
            putInt(NGO_ID, id)
            putString(NGO_NAME, name)
            commit()
        }
    }

    override fun setNGOToken(token: String) {
        sharedPreferences.edit(commit = true) {
            putString(NGO_TOKEN, token)
            commit()
        }
    }

    override fun getNGOToken(): String {
        return sharedPreferences.getString(NGO_TOKEN, "") ?: ""
    }

    override fun getNGOId(): Int {
        return sharedPreferences.getInt(NGO_ID, 0)
    }

    override fun getNGOName(): String {
        return sharedPreferences.getString(NGO_NAME, "") ?: ""
    }

    override fun getLatLong(): Pair<Double, Double> {
        return sharedPreferences.getFloat(LOCATION_LATITUDE, 6.465422F)
            .toDouble() to sharedPreferences.getFloat(LOCATION_LONGITUDE, 3.406448F).toDouble()
    }

    override fun setLatLong(latitude: Double, longitude: Double) {
        sharedPreferences.edit(commit = true) { putFloat(LOCATION_LATITUDE, latitude.toFloat()) }
        sharedPreferences.edit(commit = true) { putFloat(LOCATION_LONGITUDE, longitude.toFloat()) }
    }

    private var clearListener: onClearListener? = null

    override fun setClearListner(listener: onClearListener) {
        clearListener = listener
    }
    override fun clearPreference() {
        sharedPreferences.edit(commit = true) {
            clear()
            clearListener?.onClear()
            commit()
        }
    }

    override fun saveProfile(user: User) {
        sharedPreferences.edit(commit = true) { putString(USER_PROFILE, Gson().toJson(user)) }
    }
    override fun getProfile(): User? {
        return sharedPreferences.getString(USER_PROFILE, null)?.let {
            Gson().fromJson(it, User::class.java)
        }
    }

    companion object {
        const val NGO_ID = "NGO_ID"
        const val NGO_TOKEN = "NGO_TOKEN"
        const val USER_PROFILE = "USER_PROFILE"
        const val NGO_NAME = "NGO_NAME"
        const val LOCATION_LONGITUDE: String = "longitude"
        const val LOCATION_LATITUDE: String = "latitude"
    }
}

interface onClearListener {
    fun onClear()
}

interface PreferenceUtilInterface {
    fun setNGO(id: Int, name: String)
    fun setNGOToken(token: String)

    fun setClearListner(listener: onClearListener)

    fun getNGOToken(): String

    fun getNGOId(): Int

    fun getNGOName(): String

    fun getLatLong(): Pair<Double, Double>

    fun setLatLong(latitude: Double, longitude: Double)

    fun clearPreference()
    fun saveProfile(user: User)
    fun getProfile(): User?
}
