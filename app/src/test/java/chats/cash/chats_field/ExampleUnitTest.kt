package chats.cash.chats_field

import chats.cash.chats_field.utils.AESEncrypt
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun check_decrypt() {
        val encrypted = AESEncrypt.aesEncrypt("Hello", AESEncrypt.SECRET_KEY)
        val decrypted = AESEncrypt.aesDecrypt(encrypted, AESEncrypt.SECRET_KEY)
        assertEquals("Hello", decrypted)
    }
}