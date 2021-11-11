package com.codose.chats.utils

import com.pixplicity.easyprefs.library.Prefs

object PrefUtils {
    private const val NGO_ID = "NGO_ID"
    private const val NGO_TOKEN = "NGO_TOKEN"
    private const val NGO_NAME = "NGO_NAME"

    fun setNGO(id : Int, name : String) {
        Prefs.putInt(NGO_ID, id)
        Prefs.putString(NGO_NAME, name)
    }

    fun setNGOToken(token : String){
        Prefs.putString(NGO_TOKEN, token)
    }

    fun getNGOToken() : String {
        return Prefs.getString(NGO_TOKEN,"")
    }

    fun getNGOId() : Int{
        return Prefs.getInt(NGO_ID,0)
    }

    fun getNGOName() : String{
        return Prefs.getString(NGO_NAME,"")
    }
}