package com.codose.chats.utils

import timber.log.Timber
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64 as encrypt

object AESEncrypt {
    @JvmStatic
    fun aesEncrypt(v: String, secretKey: String) = AES256.encrypt(v, secretKey)
    @JvmStatic
    fun aesDecrypt(v: String, secretKey: String) = AES256.decrypt(v, secretKey)
    const val SECRET_KEY = "740239c6243+*9c62439c6b1d41d7402"
}

private object AES256 {

    private fun cipher(opmode: Int, secretKey: String): Cipher {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val sk = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
        val iv = IvParameterSpec(secretKey.substring(0, 16).toByteArray(Charsets.UTF_8))
        c.init(opmode, sk, iv)
        return c
    }


    fun encrypt(str: String, secretKey: String): String {
        val encrypted =
            cipher(Cipher.ENCRYPT_MODE, secretKey).doFinal(str.toByteArray(Charsets.UTF_8))
        return String(encrypt.encode(encrypted, encrypt.DEFAULT))
    }

    fun decrypt(str: String, secretKey: String): String {
        val byteStr = encrypt.decode(str.toByteArray(Charsets.UTF_8), encrypt.DEFAULT)
        return String(cipher(Cipher.DECRYPT_MODE, secretKey).doFinal(byteStr))
    }
}

class AES {
    var keyGenerator: KeyGenerator? = null
    var secretKey: SecretKey? = null
    var IV = ByteArray(16)
    var random: SecureRandom? = null

    init {
        try {
            keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator?.init(256)
            secretKey = keyGenerator?.generateKey()
            Timber.v("Decryption : ${secretKey?.encoded}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        random = SecureRandom()
        random!!.nextBytes(IV)
    }

    fun encrypt(plaintext: ByteArray?): ByteArray? {
        val cipher = Cipher.getInstance("AES")
        val keySpec = SecretKeySpec(secretKey?.encoded, "AES")
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(plaintext)
    }

    fun decrypt(cipherText: ByteArray?): String? {
        try {
            val cipher = Cipher.getInstance("AES")
            val keySpec = SecretKeySpec(secretKey?.encoded, "AES")
            val ivSpec = IvParameterSpec(IV)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decryptedText = cipher.doFinal(cipherText)
            return String(decryptedText)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }
}