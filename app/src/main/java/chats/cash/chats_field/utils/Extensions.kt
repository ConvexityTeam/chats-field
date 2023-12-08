package chats.cash.chats_field.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import chats.cash.chats_field.utils.ChatsFieldConstants.COMPLETE
import chats.cash.chats_field.utils.ChatsFieldConstants.INCOMPLETE
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

fun View?.show() {
    this?.visibility = View.VISIBLE
}

fun View?.hide() {
    this?.visibility = View.GONE
}

fun Context.toast(message: String?) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun String.toDateString(): String {
    val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val convertedDate: Date
    var formattedDate: String
    val myFormat = "dd-MM-yyyy"
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    try {
        convertedDate = sdf1.parse(this)
        formattedDate = sdf.format(convertedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
        formattedDate = this
    }
    return formattedDate
}

fun String.toDateStringII(): String {
    val sdf1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val convertedDate: Date
    var formattedDate: String
    val myFormat = "yyyy-MM-dd'T'HH:mm:ss"
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    try {
        convertedDate = sdf1.parse(this)
        formattedDate = sdf.format(convertedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
        formattedDate = this
    }
    return formattedDate
}

fun Date.toDateTimeStringII(): String {
    val formattedDate: String
    val myFormat = "yyyy-MM-dd'T'HH:mm:ss"
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    formattedDate = sdf.format(this)
    return formattedDate
}

fun String.getYouTubeId(): String {
    return try {
        val pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
        val compiledPattern: Pattern = Pattern.compile(pattern)
        val matcher: Matcher = compiledPattern.matcher(this)
        if (matcher.find()) {
            matcher.group()
        } else {
            "error"
        }
    } catch (e: Exception) {
        Timber.i(e.message!!)
        "error"
    }
}

fun Date.convertDateToString(): String {
    val myFormat = "yyyy-MM-dd" // In which you need put here
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    return sdf.format(this)
}

fun Date.convertDateTimeToString(): String {
    val myFormat = "yyyy-MM-dd hh:mm aa" // In which you need put here
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    return sdf.format(this)
}

fun String?.isValidPhoneNo(): Boolean {
    return this != null
}

fun String?.isValidPin(): Boolean {
    return this != null && this.length == 4
}

fun TextInputEditText.isValid(): Boolean {
    return !this.text.isNullOrBlank()
}

fun EditText.isValid(): Boolean {
    return !this.text.isNullOrBlank()
}

fun String.isEmailValid(): Boolean {
    return Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), this)
}

fun Bitmap.toFile(context: Context, name: String): File {
    val f = File(context.filesDir, name)
    f.createNewFile()
    val bos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 70, bos)
    val bitmapData = bos.toByteArray()
    val fos = FileOutputStream(f)
    fos.write(bitmapData)
    fos.flush()
    fos.close()
    return f
}

fun String.toFile(): File {
    return File(this)
}

@Throws(FileNotFoundException::class)
suspend fun writeBitmapToFile(
    applicationContext: Context,
    bitmap: Bitmap,
    child: String = "fingerprint_images",
): File {
    return withContext(Dispatchers.IO) {
        val name = String.format("chats-image-%s.png", UUID.randomUUID().toString())
        val outputDir = File(applicationContext.filesDir, child)
        if (!outputDir.exists()) {
            outputDir.mkdirs() // should succeed
        }
        val outputFile = File(outputDir, name)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
        } finally {
            out?.let {
                try {
                    it.close()
                } catch (ignore: IOException) {
                }
            }
        }
        return@withContext outputFile
    }
}

fun Fragment.showToast(message: String?) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun String?.toTitleCase(): String {
    return if (isNullOrBlank()) {
        ""
    } else if (length == 1) {
        uppercase()
    } else if (this.length > 1) {
        get(0).uppercase().plus(substring(1).lowercase())
    } else {
        ""
    }
}

fun Throwable.handleThrowable(
    errorMessage: String = "something went wrong",
    internetError: String = "Please check your internet connection and try again",
    socketError: String = "Pleas check your network connection. Make sure you're connected to a good network",
): String {
    Timber.e(this)
    return when (this) {
        is SocketTimeoutException -> socketError
        is UnknownHostException -> internetError
        is HttpException -> Utils.getErrorMessage(this)
        else -> {
//            FirebaseCrashlytics.getInstance().recordException(this)
            errorMessage
        }
    }
}

fun Boolean?.toStatusString(): String {
    return if (this != null) {
        if (this == true) {
            COMPLETE
        } else {
            INCOMPLETE
        }
    } else {
        INCOMPLETE
    }
}

fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                textPaint.color = Color.rgb(0, 40, 85)
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                textPaint.isUnderlineText = true
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun NavController.safeNavigate(destination: NavDirections) {
    currentDestination?.getAction(destination.actionId)?.run { navigate(destination) }
        ?: run { Timber.v("destination does not exist") }
}

fun NavController.safeNavigate(@IdRes destination: Int) {
    currentDestination?.getAction(destination)?.run { navigate(destination) }
}

fun Uri.toFile(context: Context): File? {
    try {
        val inputStream = context.contentResolver.openInputStream(this)
        // Decode the input stream into a Bitmap
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val file = File(context.cacheDir, "${Calendar.getInstance().timeInMillis}.jpg")
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.close()
        }
        inputStream?.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
