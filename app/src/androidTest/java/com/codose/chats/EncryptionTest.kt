package com.codose.chats

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import chats.cash.chats_field.utils.encryption.AES256Encrypt
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptionTest {


    @Test
    fun doesStringEncryptandDecrypt(){
        val encryptionTest = AES256Encrypt("zXCgF295h5YB2E(cDhOBx!s")
        val encryptedString = encryptionTest.encrypt("simon",)
        Log.d("string",encryptedString.toString())
        assert(encryptedString!="simon")

        //DECRYPTION
        val decrypted = encryptionTest.decrypt(encryptedString)
        println(decrypted)
        assert(decrypted == "simon")
    }



    @Test
    fun doesStringEncryptandDecryptFailWhenWrongKeyUsed(){
        val encryptionTest = AES256Encrypt("zXCgF295h5YB2E(cDhOBx!s")
        val encryptedString = encryptionTest.encrypt("simon",)
        Log.d("string",encryptedString.toString())
        assert(encryptedString!="simon")

        //DECRYPTION
        val encryptionTest2 = AES256Encrypt("zXCrF295h5YB2E(cDhOBx!s")
        val decrypted = encryptionTest2.decrypt(encryptedString)
        println(decrypted)
        assert(decrypted != "simon")
    }





}