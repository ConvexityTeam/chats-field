package chats.cash.chats_field.utils

import android.graphics.Bitmap
import chats.cash.chats_field.utils.ChatsFieldConstants.sDirectory
import okhttp3.internal.and
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.experimental.or

object FingerPrintUtils {
    /**
     * generate the fingerprint image
     * @param data image data
     * @param width width of the image
     * @param height height of the image
     * @param offset default setting as 0
     * @return bitmap image data
     */
    fun getFingerprintImage(
        data: ByteArray?,
        width: Int,
        height: Int,
        offset: Int,
    ): ByteArray? {
        if (data == null) {
            return null
        }
        val imageData = ByteArray(width * height)
        for (i in 0 until width * height / 2) {
            imageData[i * 2] = (data[i + offset] and 0xf0).toByte()
            imageData[i * 2 + 1] = (data[i + offset] or 4 and 0xf0).toByte()
        }
        return toBmpByte(width, height, imageData)
    }

    private fun toBmpByte(width: Int, height: Int, data: ByteArray): ByteArray? {
        var buffer: ByteArray? = null
        try {
            val baos = ByteArrayOutputStream()
            val dos = DataOutputStream(baos)
            val bfType = 0x424d
            val bfSize = 54 + 1024 + width * height
            val bfReserved1 = 0
            val bfReserved2 = 0
            val bfOffBits = 54 + 1024
            dos.writeShort(bfType)
            dos.write(changeByte(bfSize), 0, 4)
            dos.write(changeByte(bfReserved1), 0, 2)
            dos.write(changeByte(bfReserved2), 0, 2)
            dos.write(changeByte(bfOffBits), 0, 4)
            val biSize = 40
            val biPlanes = 1
            val biBitcount = 8
            val biCompression = 0
            val biSizeImage = width * height
            val biXPelsPerMeter = 0
            val biYPelsPerMeter = 0
            val biClrUsed = 256
            val biClrImportant = 0
            dos.write(changeByte(biSize), 0, 4)
            dos.write(changeByte(width), 0, 4)
            dos.write(changeByte(height), 0, 4)
            dos.write(changeByte(biPlanes), 0, 2)
            dos.write(changeByte(biBitcount), 0, 2)
            dos.write(changeByte(biCompression), 0, 4)
            dos.write(changeByte(biSizeImage), 0, 4)
            dos.write(changeByte(biXPelsPerMeter), 0, 4)
            dos.write(changeByte(biYPelsPerMeter), 0, 4)
            dos.write(changeByte(biClrUsed), 0, 4)
            dos.write(changeByte(biClrImportant), 0, 4)
            val palatte = ByteArray(1024)
            for (i in 0..255) {
                palatte[i * 4] = i.toByte()
                palatte[i * 4 + 1] = i.toByte()
                palatte[i * 4 + 2] = i.toByte()
                palatte[i * 4 + 3] = 0
            }
            dos.write(palatte)
            dos.write(data)
            dos.flush()
            buffer = baos.toByteArray()
            dos.close()
            baos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return buffer
    }

    private fun changeByte(data: Int): ByteArray? {
        val b4 = (data shr 24).toByte()
        val b3 = (data shl 8 shr 24).toByte()
        val b2 = (data shl 16 shr 24).toByte()
        val b1 = (data shl 24 shr 24).toByte()
        return byteArrayOf(b1, b2, b3, b4)
    }

    /**
     * method for saving the fingerprint image as JPG
     * @param bitmap bitmap image
     */
    fun saveJPGimage(bitmap: Bitmap) {
        val dir = sDirectory
        val imageFileName = System.currentTimeMillis().toString()
        try {
            val file = File("$dir$imageFileName.jpg")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * method of copying the byte[] data with specific length
     * @param dstbuf byte[] for storing the copied data with specific length
     * @param dstoffset the starting point for storing
     * @param srcbuf the source byte[] used for copying.
     * @param srcoffset the starting point for copying
     * @param size the length required to copy
     */
    fun memcpy(
        dstbuf: ByteArray,
        dstoffset: Int,
        srcbuf: ByteArray,
        srcoffset: Int,
        size: Int,
    ) {
        for (i in 0 until size) {
            dstbuf[dstoffset + i] = srcbuf[srcoffset + i]
        }
    }
}
