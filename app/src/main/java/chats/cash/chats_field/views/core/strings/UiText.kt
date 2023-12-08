package chats.cash.chats_field.views.core.strings

import android.content.Context
import androidx.annotation.StringRes

class UiText(private val context: Context) {

    fun getStringResource(
        @StringRes id: Int,
    ): String {
        return context.getString(id)
    }

    fun getStringResource(
        @StringRes id: Int,
        args: List<Any>,
    ): String {
        return context.getString(id, *args.toTypedArray())
    }
}
