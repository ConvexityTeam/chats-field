package com.codose.chats.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.google.android.material.textfield.TextInputEditText
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

fun View.show(){
    this.visibility = View.VISIBLE
}

fun View.hide(){
    this.visibility = View.GONE
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun String.toDateString() : String{
    val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val convertedDate: Date
    var formattedDate: String
    val myFormat = "yyyy-MM-dd" //In which you need put here
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

fun String.toDateStringII() : String{
    val sdf1 = SimpleDateFormat("yyyy-MM-dd")
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

fun Date.toDateTimeStringII() : String{
    val formattedDate : String
    val myFormat = "yyyy-MM-dd'T'HH:mm:ss"
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    formattedDate = sdf.format(this)
    return formattedDate
}

fun String.getYouTubeId() : String{
    return try{
        val pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
        val compiledPattern: Pattern = Pattern.compile(pattern)
        val matcher: Matcher = compiledPattern.matcher(this)
        if (matcher.find()) {
            matcher.group()
        } else {
            "error"
        }
    }catch (e : Exception){
        Log.i("YOUTUBE ERROR :",e.message!!)
        "error"
    }
}

fun Date.convertDateToString() : String{
    val myFormat = "yyyy-MM-dd" //In which you need put here
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    return sdf.format(this)
}

fun Date.convertDateTimeToString() : String{
    val myFormat = "yyyy-MM-dd hh:mm aa" //In which you need put here
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    return sdf.format(this)
}

fun String?.isValidPhoneNo(): Boolean {
    return this != null && this.length >= 7 && this.length <= 17
}

fun TextInputEditText.isValid() : Boolean{
    return !this.text.isNullOrBlank()
}

fun Bitmap.toFile(context : Context, name : String) : File {
    val f = File(context.cacheDir, name);
    f.createNewFile()
    val bos =  ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 50 , bos)
    val bitmapdata = bos.toByteArray()
    val fos =  FileOutputStream(f)
    fos.write(bitmapdata)
    fos.flush()
    fos.close()
    return f
}

fun String.toFile() : File{
    return  File(this)
}

@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap, child : String = "fingerprint_images"): File {
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
    return outputFile
}

