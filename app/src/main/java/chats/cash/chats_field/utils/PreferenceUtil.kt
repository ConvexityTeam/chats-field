package chats.cash.chats_field.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import chats.cash.chats_field.utils.BluetoothConstants.SHARED_PREFERENCE_NAME

class PreferenceUtil(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun setNGO(id: Int, name: String) {
        sharedPreferences.edit(commit = true) {
            putInt(NGO_ID, id)
            putString(NGO_NAME, name)
        }
    }

    fun setNGOToken(token: String) {
        sharedPreferences.edit(commit = true) { putString(NGO_TOKEN, token) }
    }

    fun getNGOToken(): String {
        return sharedPreferences.getString(NGO_TOKEN, "") ?: ""
    }

    fun getNGOId(): Int {
        return sharedPreferences.getInt(NGO_ID, 0)
    }

    fun getNGOName(): String {
        return sharedPreferences.getString(NGO_NAME, "") ?: ""
    }

    fun clearPreference() {
        sharedPreferences.edit(commit = true) { clear() }
    }

    companion object {
        const val NGO_ID = "NGO_ID"
        const val NGO_TOKEN = "NGO_TOKEN"
        const val NGO_NAME = "NGO_NAME"
    }
}
