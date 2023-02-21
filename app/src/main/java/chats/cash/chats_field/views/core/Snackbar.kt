package chats.cash.chats_field.views.core

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import chats.cash.chats_field.R
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

fun showSnackbarWithAction(text: String, root: View, action:Int,
color: Int? =null,onClick: () -> Unit,){
    val snackbar = Snackbar.make(root,text, Snackbar.LENGTH_INDEFINITE)
    snackbar.setAction(action){
        onClick()
        snackbar.dismiss()
    }
    color?.let {
        snackbar.setBackgroundTint(it)
    }
    snackbar.show()
}


fun showSuccessSnackbar(text: Int, root: View,
                        color: Int? = ContextCompat.getColor(root.context, R.color.colorPrimary),){
    val snackbar = Snackbar.make(root,root.context.getString(text), Snackbar.LENGTH_INDEFINITE)

    color?.let {
        snackbar.setBackgroundTint(it)
    }
    snackbar.show()
}
fun showSuccessSnackbar(text: String, root: View,
                        color: Int? = ContextCompat.getColor(root.context, R.color.colorPrimary),){
    val snackbar = Snackbar.make(root,text, Snackbar.LENGTH_INDEFINITE)

    color?.let {
        snackbar.setBackgroundTint(it)
    }
    snackbar.show()
}

fun showErrorSnackbar(text: Int, root: View,
                        color: Int? = ContextCompat.getColor(root.context, R.color.design_default_color_error),){
    val snackbar = Snackbar.make(root,root.context.getString(text), Snackbar.LENGTH_INDEFINITE)

    color?.let {
        snackbar.setBackgroundTint(it)
    }
    snackbar.show()
}