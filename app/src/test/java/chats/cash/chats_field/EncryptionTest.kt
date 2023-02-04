package chats.cash.chats_field

import android.util.Log
import chats.cash.chats_field.utils.AES
import chats.cash.chats_field.utils.AESEncrypt
import chats.cash.chats_field.utils.encryption.AESEncrption
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class EncryptionTest {

    val keygen = KeyGenerator.getInstance("AES")


    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

    lateinit var key:SecretKey
    @Before
    fun setUp( ){
        keygen.init(256)
        key= keygen.generateKey()

    }

    val stringToEncrypt ="hello my name is simon"
    var byteToDecrpt:ByteArray?=null
    @Test
    fun `Does String Encrypt and Decrypt`(){
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = AESEncrption.encrypt(stringToEncrypt,cipher)
        byteToDecrpt = encrypted
        val encryptedString = encrypted.toString(Charsets.UTF_8)
        println(encryptedString)
        assert(encryptedString!=stringToEncrypt)

        //DECRYPTION
        val decrypted = AESEncrption.decrypt(byteToDecrpt!!, IvParameterSpec(cipher.iv), key)
        val decryptedString = decrypted.toString(Charsets.UTF_8)
        println(decryptedString)
        assert(decryptedString == stringToEncrypt)

    }


    @After
    fun tearDown(){
        byteToDecrpt = null
    }



}