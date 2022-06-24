package com.codose.chats.network.api

import android.content.Context
import android.content.SharedPreferences
import com.codose.chats.R

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.app_name), Context.MODE_PRIVATE
    )

    companion object {
        const val FIELD_AGENT_LOGIN_TOKEN = "field_agent_token"
    }

    /*
    * Function to save auth token
    * */
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(FIELD_AGENT_LOGIN_TOKEN, token)
        editor.apply()
    }

    /*
    * Function to save auth token
    */
    fun fetchToken() : String? {
        return prefs.getString(FIELD_AGENT_LOGIN_TOKEN, null)
    }
}