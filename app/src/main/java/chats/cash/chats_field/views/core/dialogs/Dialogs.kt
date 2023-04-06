package chats.cash.chats_field.views.core.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import androidx.annotation.DrawableRes
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.PermissionDialogsBinding

fun Activity.getPermissionDialogs(title:Int = R.string.we_need_this_permissoin, desc:Int, @DrawableRes icon:Int,
                                  onDismiss:()-> Unit,
    onRequest:()-> Unit):AlertDialog {

    val dialog = AlertDialog.Builder(this)
    val binding = PermissionDialogsBinding.inflate(LayoutInflater.from(this))
    dialog.setCancelable(false)
    dialog.setView(binding.root)
    val alertDialog = dialog.create()
    binding.apply {
        binding.title.text = getString(title)
        binding.desc.text = getString(desc)
        permissionIcon.setImageResource(icon)
        accept.setOnClickListener {
            alertDialog.dismiss()
            onRequest()
        }
        decline.setOnClickListener {
            onDismiss()
            alertDialog.dismiss()
        }
    }
    return alertDialog
}