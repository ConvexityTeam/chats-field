package chats.cash.chats_field.utils

object ChatsFieldCommands {

    /**
     * calculate the check sum of the byte[]
     * @param buffer byte[] required for calculating
     * @param size the size of the byte[]
     * @return the calculated check sum
     */
    fun calcCheckSum(buffer: ByteArray, size: Int): Int {
        var sum = 0
        for (i in 0 until size) {
            sum += buffer[i]
        }
        return sum and 0x00ff
    }
}
