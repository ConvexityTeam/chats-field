package chats.cash.chats_field.utils.encryption

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class AES256Encrypt(key: String) {
    /* Private variable declaration */
    private val SECRET_KEY = key
    private val SALTVALUE = key

    /* Encryption Method */
    fun encrypt(strToEncrypt: String): String? {
        try {
            /* Declare a byte array. */
            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            val ivspec = IvParameterSpec(iv)
            /* Create factory for secret keys. */
            val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            /* PBEKeySpec class implements KeySpec interface. */
            val spec: KeySpec =
                PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.toByteArray(), 65536, 256)
            val tmp: SecretKey = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.getEncoded(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec)
            /* Retruns encrypted value. */return Base64
                .encodeToString(cipher.doFinal(strToEncrypt.toByteArray(StandardCharsets.UTF_8)), 0)
        } catch (e: InvalidAlgorithmParameterException) {
            println("Error occured during encryption: " + e.toString())
        } catch (e: InvalidKeyException) {
            println("Error occured during encryption: " + e.toString())
        } catch (e: NoSuchAlgorithmException) {
            println("Error occured during encryption: " + e.toString())
        } catch (e: InvalidKeySpecException) {
            println("Error occured during encryption: " + e.toString())
        } catch (e: BadPaddingException) {
            println("Error occured during encryption: " + e.toString())
        } catch (e: IllegalBlockSizeException) {
            println("Error occured during encryption: " + e.toString())
        } catch (e: NoSuchPaddingException) {
            println("Error occured during encryption: " + e.toString())
        }
        return null
    }

    /* Decryption Method */
    fun decrypt(strToDecrypt: String?): String? {
        try {
            /* Declare a byte array. */
            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            val ivspec = IvParameterSpec(iv)
            /* Create factory for secret keys. */
            val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            /* PBEKeySpec class implements KeySpec interface. */
            val spec: KeySpec =
                PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.toByteArray(), 65536, 256)
            val tmp: SecretKey = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.getEncoded(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec)
            /* Retruns decrypted value. */return String(
                cipher.doFinal(
                    Base64.decode(strToDecrypt, 0),
                ),
            )
        } catch (e: InvalidAlgorithmParameterException) {
            println("Error occured during decryption: " + e.toString())
        } catch (e: InvalidKeyException) {
            println("Error occured during decryption: " + e.toString())
        } catch (e: NoSuchAlgorithmException) {
            println("Error occured during decryption: " + e.toString())
        } catch (e: InvalidKeySpecException) {
            println("Error occured during decryption: " + e.toString())
        } catch (e: BadPaddingException) {
            println("Error occured during decryption: " + e.toString())
        } catch (e: IllegalBlockSizeException) {
            println("Error occured during decryption: " + e.toString())
        } catch (e: NoSuchPaddingException) {
            println("Error occured during decryption: " + e.toString())
        }
        return null
    }
}
