package org.xfort.xrock.crypto

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.experimental.xor

/**
 ** Created by ZhangHuaXin on 2021/7/27.
 * 异或加解密
 **/
class XORHelper {
    val BUFFER_SIZE = 4 * 1024

    fun xorBytes(data: ByteArray, key: ByteArray): ByteArray {
        val resData = ByteArray(data.size)
        data.forEachIndexed { index, byte ->
            val keyByte = key[index % key.size]
            resData[index] = byte.xor(keyByte)
        }
        return resData
    }

    fun xorFile(dataFile: File, outFile: File, key: ByteArray): Boolean {
        val outFile = FileOutputStream(outFile)
        val fileChannel = FileInputStream(dataFile).channel
        val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
        var ok = true
        try {
            while (fileChannel.read(buffer) != -1) {
                buffer.flip()
                outFile.write(xorBytes(buffer.array(), key))
                buffer.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ok = false
        } finally {
            fileChannel.close()
            outFile.close()
        }
        return ok
    }
}