package chats.cash.chats_field

import java.util.Base64

class Base64 {
    companion object {
        fun encodeToString(input: ByteArray?, flags: Int): String {
            return Base64.getEncoder().encodeToString(input)
        }

        fun decode(str: String?, flags: Int): ByteArray {
            return Base64.getDecoder().decode(str)
        } // add other methods if required...
    }
}