package chats.cash.chats_field.utils.dialogs

import android.app.Dialog
import android.content.Context
import android.view.Window
import androidx.appcompat.app.AlertDialog
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.LayoutErrorDialogBinding
import chats.cash.chats_field.databinding.LayoutSuccessDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs


fun AlertDialog(context: Context,title:String, message:String,
postiveText:String = "Yes", negativeText:String="Cancel", onNegativeClicked:()->Unit={},
                onPostiveClicked:()-> Unit):MaterialAlertDialogBuilder{
   return MaterialAlertDialogBuilder(context)
        .setTitle(title)
       .setOnCancelListener {
           onNegativeClicked()
       }
        .setMessage(message)
        .setPositiveButton(
            postiveText
        ) { dialogInterface, i ->
            onPostiveClicked()
            dialogInterface.dismiss()
        }
        .setNegativeButton(
            negativeText
        ) { dialogInterface, i ->
            onNegativeClicked()
            dialogInterface.dismiss()
        }

}


    fun getErrorDialog(title: String,context: Context, onDismissClicked:()-> Unit = {}):Dialog{
         val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val binding = LayoutErrorDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setOnDismissListener {
            onDismissClicked()
        }
        val body =binding.title
        body.text = title
        binding.dismiss.setOnClickListener {
            dialog.dismiss()
            onDismissClicked()
        }
        return dialog }


fun getSuccessDialog(title: String,context: Context, onDismissClicked:()-> Unit = {}):Dialog{
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setOnDismissListener {
        onDismissClicked()
    }
    val binding = LayoutSuccessDialogBinding.inflate(dialog.layoutInflater)
    dialog.setContentView(binding.root)
    val body =binding.title
    body.text = title
    binding.dismiss.setOnClickListener {
        dialog.dismiss()
        onDismissClicked()
    }
    return dialog }
