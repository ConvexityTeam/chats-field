package chats.cash.chats_field.views.core

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun showSnackbarWithAction(text: Int, root: View, action:Int,
color: Int? =null,onClick: () -> Unit,){
    val snackbar = Snackbar.make(root,root.context.getString(text), Snackbar.LENGTH_INDEFINITE)
    snackbar.setAction(action){
        onClick()
        snackbar.dismiss()
    }
    color?.let {
        snackbar.setBackgroundTint(it)
    }
    snackbar.show()
}