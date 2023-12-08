package chats.cash.chats_field.views.core.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ConfirmDialogBinding
import chats.cash.chats_field.databinding.PermissionDialogsBinding
import chats.cash.chats_field.databinding.SuccessDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Activity.getPermissionDialogs(
    title: Int = R.string.we_need_this_permissoin,
    desc: Int,
    @DrawableRes icon: Int,
    onDismiss: () -> Unit,
    onRequest: () -> Unit,
): AlertDialog {
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

fun Context.showAlertDialog(
    title: Int = R.string.we_need_this_permissoin,
    desc: Int,
    @DrawableRes icon: Int = R.drawable.baseline_warning_amber_24,
    positiveText: Int = R.string.logout,
    onDismiss: () -> Unit = {},
    onPositiveButtonClicked: () -> Unit,
): androidx.appcompat.app.AlertDialog {
    val dialog = MaterialAlertDialogBuilder(this)
    val binding = ConfirmDialogBinding.inflate(LayoutInflater.from(this))
    dialog.setCancelable(false)
    dialog.setView(binding.root)
    val alertDialog = dialog.create()
    binding.apply {
        binding.title.text = getString(title)
        binding.descc.text = getString(desc)
        iconImageView.setImageResource(icon)
        positiveButton.text = getString(positiveText)
        positiveButton.setOnClickListener {
            alertDialog.dismiss()
            onPositiveButtonClicked()
        }
        negativeButton.setOnClickListener {
            onDismiss()
            alertDialog.dismiss()
        }
    }
    return alertDialog
}

fun Context.showSuccessDialog(
    title: Int = R.string.we_need_this_permissoin,
    desc: Int,
    onPositiveButtonClicked: () -> Unit,
): androidx.appcompat.app.AlertDialog {
    val dialog = MaterialAlertDialogBuilder(this)
    val binding = SuccessDialogBinding.inflate(LayoutInflater.from(this))
    dialog.setCancelable(false)
    dialog.setView(binding.root)
    val alertDialog = dialog.create()
    binding.apply {
        binding.title.text = getString(title)
        binding.descc.text = getString(desc)
        dismissButton.setOnClickListener {
            alertDialog.dismiss()
            onPositiveButtonClicked()
        }
    }
    return alertDialog
}
