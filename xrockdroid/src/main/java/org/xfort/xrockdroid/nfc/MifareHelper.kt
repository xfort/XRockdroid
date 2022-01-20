package org.xfort.xrockdroid.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import androidx.collection.SparseArrayCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

/**
 ** Created by ZhangHuaXin on 2021/6/18.
 *
 * NFC读卡器
 **/
object MifareHelper {
    val NO_DATA = "----------------".toByteArray()
    val NO_KEY = "------".toByteArray()
    val DEFAULT_KEY = hex2Bytes("FFFFFFFFFFFF")


    /**
     * intent 中无 tag 数据
     */
    const val ERROR = -1

    /**
     * intent 中无 tag 数据
     */
    const val INTENT_TAG_NULL = -2

    /**
     * tag数据异常，无法解析
     */
    const val TAG_DATA_ERROR = -3

    /**
     * 手机不支持此卡
     */
    const val DEVICE_NOT_SUPPORT = -11

    /**
     * tag不支持
     */
    const val TAG_NOT_SUPPORT = -12

    /**
     * 创建 mc reader失败
     */
    const val CREATE_MC_ERROR = -20

    /**
     * MC 连接失败
     */
    const val CONNECT_ERROR = -21

    /**
     * MC 连接超时
     */
    const val CONNECT_TIMEOUT = -22


    /**
     * sector index 越界
     */
    const val SECTOR_OUT = -30

    /**
     * block index 越界
     */
    const val BLOCK_OUT = -31


    /**
     * block data 数据长度必须==16
     */
    const val BLOCK_DATA_ERROR = -32

    /**
     * key 错误
     */
    const val KEY_AUTH_FAIL = -33

    /**
     * 写数据失败
     */
    const val WRITE_BLOCK_ERROR = -34


    /**
     * 读数据失败
     */
    const val READ_ERROR = -35

    var mMFC: MifareClassic? = null
    var nfcTag: Tag? = null
    var  uid:String?=null


    fun parseNFCIntent(intent: Intent): Int {
        if (NfcAdapter.ACTION_TECH_DISCOVERED != intent.action) {
            return -1
        }
        var tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return INTENT_TAG_NULL
        var newTag: Tag? = patchTag(tag) ?: return TAG_DATA_ERROR

        nfcTag = newTag
        var resCode = 0
        nfcTag?.let {
            uid= bytes2Hex(it.id)
            resCode = checkSupport(it)
        }
        return resCode
    }

    fun conntected(): Boolean {
        try {
            return mMFC != null && mMFC?.isConnected == true
        } catch (e: java.lang.RuntimeException) {
            e.printStackTrace()
        }

        return false
    }

    suspend fun readSectors(firstSector: Int, lastSector: Int, keysA: Array<ByteArray>, keysB: Array<ByteArray>)
            : SparseArrayCompat<Array<ByteArray>?>? {
        var code = checkForTagAndCreate()
        if (code != 0) {
            Log.e(javaClass.name, "readSectors()_$code")
            return null
        }
        val sectorKeyMap = buildKeyMap(firstSector, lastSector, keysA, keysB)
        return readSectors(sectorKeyMap)
    }

    fun readSectors(sectorKeyMap: SparseArrayCompat<Array<ByteArray>>): SparseArrayCompat<Array<ByteArray>?> {
        val startTime=System.currentTimeMillis()
        var resData = SparseArrayCompat<Array<ByteArray>?>()
        val size=sectorKeyMap.size()
        for(index in 0 until size){
            val sectorIndex=sectorKeyMap.keyAt(index)
            val keyAB=sectorKeyMap[sectorIndex]
            val sectorBytes = readSector(sectorIndex, keyAB!!)
            resData.put(sectorIndex, sectorBytes)
        }
        Log.d("NFC","readSectors() duration "+(System.currentTimeMillis()-startTime))
        return resData
    }

    @Throws(TagLostException::class)
    fun readSector(sectorIndex: Int, keyAB: Array<ByteArray>): Array<ByteArray>? {
        mMFC?.let { mc ->
            var blocksArray = arrayOf<Array<ByteArray>?>(emptyArray(), emptyArray())
            keyAB.forEachIndexed { index, keyBytes ->
                if (keyBytes.isNotEmpty()) {
                    blocksArray[index] = readSector(sectorIndex, keyBytes, index == 1)
                }
            }
            var blocksData = mutableListOf<ByteArray>()

            blocksArray.forEachIndexed { keyIndex, blocks ->
                if (blocks != null && blocks.isNotEmpty()) {
                    blocks.forEachIndexed { blockIndex, bytes ->
                        val itemBlock = blocksData.getOrNull(blockIndex)
                        if (itemBlock == null) {
                            blocksData.add(bytes)
                        } else if (itemBlock.contentEquals(NO_DATA) && !bytes.contentEquals(NO_DATA)) {
                            blocksData[blockIndex] = bytes
                        } else if (blockIndex == blocks.size - 1 && !itemBlock.contentEquals(NO_DATA) && !bytes
                                .contentEquals(NO_DATA)
                        ) {
                            var keyABytes = itemBlock.copyOfRange(0, 6)
                            blocksData[blockIndex] = byteArrayOf(*keyABytes, *bytes.copyOfRange(6, 16))
                        }
                    }
                }
            }
            return blocksData.toTypedArray()
        }
        return null
    }

    private fun readSector(sectorIndex: Int, keyBytes: ByteArray, isKeyB: Boolean): Array<ByteArray>? {
        if (mMFC == null) {
            return null
        }
        var auth = authSectorKey(sectorIndex, keyBytes, isKeyB)
        if (!auth) {
            return null
        }

        var firstBlock = mMFC!!.sectorToBlock(sectorIndex)
        var lastBlock = firstBlock + 4
        if (mMFC!!.size == MifareClassic.SIZE_4K && sectorIndex > 31) {
            lastBlock = firstBlock + 16
        }
        var sectorBlockList = mutableListOf<ByteArray>()

        for (index in firstBlock until lastBlock) {
            try {
                var blockBytes = mMFC!!.readBlock(index)
                if (blockBytes.size != 16) {
                    Log.w(
                        javaClass.name,
                        "readSector()_$sectorIndex _ $index \n" + bytes2Hex(
                            blockBytes
                        )
                    )
                }
                sectorBlockList.add(blockBytes)
            } catch (e: IOException) {
                e.printStackTrace()
                sectorBlockList.add(NO_DATA)
                if (!mMFC!!.isConnected) {
                    throw TagLostException("Tag removed during readSector(...)")
                }
            }
        }

        if (sectorBlockList.isNotEmpty()) {
            val lastIndex = sectorBlockList.size - 1
            var noData = true
            sectorBlockList.forEachIndexed { index, bytes ->
                if (!bytes.contentEquals(NO_DATA)) {
                    noData = false
                    return@forEachIndexed
                }
            }
            if (noData) {
                sectorBlockList.clear()
            } else {
                if (isKeyB) {
                    sectorBlockList[lastIndex] = byteArrayOf(
                        *(NO_KEY),
                        *(sectorBlockList[lastIndex].copyOfRange(6, 10)),
                        *keyBytes
                    )
                } else {
                    if (isKeyBReadable(
                            sectorBlockList[lastIndex].copyOfRange(
                                6,
                                10
                            )
                        )
                    ) {
                        sectorBlockList[lastIndex] = byteArrayOf(
                            *(keyBytes),
                            *(sectorBlockList[lastIndex].copyOfRange(6, 16))
                        )
                    } else {
                        sectorBlockList[lastIndex] = byteArrayOf(
                            *(keyBytes),
                            *(sectorBlockList[lastIndex].copyOfRange(6, 10)),
                            *NO_KEY
                        )
                    }
                }
            }
        }
        return sectorBlockList.toTypedArray()
    }

    private suspend fun checkForTagAndCreate(): Int {
        var code = 0
        nfcTag?.let {
            try {
                var tmpMFC = MifareClassic.get(it)
                tmpMFC?.let {
                    mMFC = it
                    code = connect()
                    if (mMFC?.isConnected == true) {

                    } else {
                        close()
                    }
                } ?: {
                    code = CREATE_MC_ERROR
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                code = CREATE_MC_ERROR
            }
        } ?: {
            code = ERROR
        }
        return code
    }

    private suspend fun connect(): Int {
        if (mMFC?.isConnected == true) {
            return 0
        }
        var code = 0
        val res = withTimeoutOrNull(1000) {
            try {
                mMFC?.connect()
                "Done"
            } catch (e: Exception) {
                e.printStackTrace()
                code = CONNECT_ERROR
            }
        }
        if (res == null) {
            code = CONNECT_TIMEOUT
        }
        return code
    }

    fun close() {
        mMFC?.let {
            it.close()
        }
    }

    /**
     * 解析原始tag，并添加新数据
     */
    fun patchTag(tag: Tag?): Tag? {
        if (tag == null) {
            return null
        }
        val techList = tag.techList
        val oldParcel = Parcel.obtain()
        tag.writeToParcel(oldParcel, 0)
        oldParcel.setDataPosition(0)

        val len = oldParcel.readInt()
        var id = ByteArray(0)
        if (len >= 0) {
            id = ByteArray(len)
            oldParcel.readByteArray(id)
        }
        val oldTechList = IntArray(oldParcel.readInt())
        oldParcel.readIntArray(oldTechList)
        val oldTechExtras = oldParcel.createTypedArray(Bundle.CREATOR)
        val serviceHandle = oldParcel.readInt()
        val isMock = oldParcel.readInt()
        val tagService: IBinder?
        tagService = if (isMock == 0) {
            oldParcel.readStrongBinder()
        } else {
            null
        }
        oldParcel.recycle()

        var nfcaIdx = -1
        var mcIdx = -1
        var sak: Short = 0
        var isFirstSak = true

        for (i in techList.indices) {
            if (techList[i] == NfcA::class.java.name) {
                if (nfcaIdx == -1) {
                    nfcaIdx = i
                }
                if (oldTechExtras!![i] != null
                    && oldTechExtras!![i]!!.containsKey("sak")
                ) {
                    sak = (sak.toInt()
                            or oldTechExtras!![i]!!.getShort("sak").toInt()).toShort()
                    isFirstSak = nfcaIdx == i
                }
            } else if (techList[i] == MifareClassic::class.java.name) {
                mcIdx = i
            }
        }
        var modified = false
        // Patch the double NfcA issue (with different SAK) for
        // Sony Z3 devices.
        if (!isFirstSak) {
            oldTechExtras!![nfcaIdx]!!.putShort("sak", sak)
            modified = true
        }
        // Patch the wrong index issue for HTC One devices.
        if (nfcaIdx != -1 && mcIdx != -1 && oldTechExtras!![mcIdx] == null) {
            oldTechExtras!![mcIdx] = oldTechExtras!![nfcaIdx]
            modified = true
        }

        if (!modified) {
            // Old tag was not modivied. Return the old one.
            return tag
        }

        // Old tag was modified. Create a new tag with the new data.
        val newParcel = Parcel.obtain()
        newParcel.writeInt(id.size)
        newParcel.writeByteArray(id)
        newParcel.writeInt(oldTechList.size)
        newParcel.writeIntArray(oldTechList)
        newParcel.writeTypedArray(oldTechExtras, 0)
        newParcel.writeInt(serviceHandle)
        newParcel.writeInt(isMock)
        if (isMock == 0) {
            newParcel.writeStrongBinder(tagService)
        }
        newParcel.setDataPosition(0)
        val newTag = Tag.CREATOR.createFromParcel(newParcel)
        newParcel.recycle()
        return newTag
    }

    /**
     * 检查是否支持 卡片、协议
     *
     *  * @return <ul>
     * <li>0 - Device and tag support MIFARE Classic.</li>
     * <li>-11 - Device does not support MIFARE Classic.</li>
     * <li>-12 - Tag does not support MIFARE Classic.</li>
     * <li>-13 - Error (tag or context is null).</li>
     */
    fun checkSupport(tag: Tag): Int {
        if (tag.techList.contains(MifareClassic::class.java.name)) {
            // Device and tag should support MIFARE Classic.
            // But is there something wrong the the tag?
            try {
                MifareClassic.get(tag)
            } catch (ex: RuntimeException) {
                // Stack incorrectly reported a MifareHelper.
                // Most likely not a MIFARE Classic tag.
                // See: https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/nfc/tech/MifareClassic.java#196
                return TAG_NOT_SUPPORT
            }
            return 0
            // This is no longer valid. There are some devices (e.g. LG's F60)
            // that have this system feature but no MIFARE Classic support.
            // (The F60 has a Broadcom NFC controller.)
            /*
        } else if (context.getPackageManager().hasSystemFeature(
                "com.nxp.mifare")){
            // Tag does not support MIFARE Classic.
            return -2;
        */
        } else {
            // Check if device does not support MIFARE Classic.
            // For doing so, check if the SAK of the tag indicate that
            // it's a MIFARE Classic tag.
            // See: https://www.nxp.com/docs/en/application-note/AN10834.pdf (page 7)
            val nfca = NfcA.get(tag)
            val sak = nfca.sak.toByte()
            return if (sak.toInt() shr 1 and 1 == 1) {
                // RFU.
                TAG_NOT_SUPPORT
            } else {
                if (sak.toInt() shr 3 and 1 == 1) { // SAK bit 4 = 1?
                    if (sak.toInt() shr 4 and 1 == 1) { // SAK bit 5 = 1?
                        // MIFARE Classic 2K
                        // MIFARE Classic 4K
                        // MIFARE SmartMX 4K
                        // MIFARE Plus S 4K SL1
                        // MIFARE Plus X 4K SL1
                        // MIFARE Plus EV1 2K/4K SL1
                        DEVICE_NOT_SUPPORT
                    } else {
                        if (sak.toInt() and 1 == 1) { // SAK bit 1 = 1?
                            // MIFARE Mini
                            DEVICE_NOT_SUPPORT
                        } else {
                            // MIFARE Classic 1k
                            // MIFARE SmartMX 1k
                            // MIFARE Plus S 2K SL1
                            // MIFARE Plus X 2K SL1
                            // MIFARE Plus SE 1K
                            // MIFARE Plus EV1 2K/4K SL1
                            DEVICE_NOT_SUPPORT
                        }
                    }
                } else {
                    // Some MIFARE tag, but not Classic or Classic compatible.
                    TAG_NOT_SUPPORT
                }
            }
        }
    }

    /**
     * 生成 各扇区的 keyA,keyB
     */
    suspend fun buildKeyMap(
        firstSector: Int,
        lastSector: Int,
        keysA: Array<ByteArray>,
        keysB: Array<ByteArray>
    ): SparseArrayCompat<Array<ByteArray>> {
        val startTime=System.currentTimeMillis()
        var keysMap = SparseArrayCompat<Array<ByteArray>>()

        var code=0
        if(!conntected()){
            code= checkForTagAndCreate()
        }

        if(code!=0){
            Log.e(javaClass.name,"buildKeyMap() error $code")
            return keysMap
        }
        val sectorCount=mMFC?.sectorCount?:0
        if(sectorCount<=0){
            Log.e(javaClass.name,"buildKeyMap() sectorCount=0")
            return  keysMap
        }
        val lastSectorIndex=Math.min(sectorCount-1,lastSector)
        for (index in firstSector..lastSectorIndex) {
            try {
                var keyPair = keysMap.get(index, Array<ByteArray>(2) { byteArrayOf() })
                keysMap.put(index, keyPair)
                if (keyPair[0].isEmpty()) {
                run loop@{
                    keysA.forEach {
                        Log.d("NFC","buildKeyMap()A$index _"+ bytes2Hex(it))
                        val auth = mMFC?.authenticateSectorWithKeyA(index, it)
                        if (auth == true) {
                            keyPair[0] = it
                            return@loop
                        }
                    }
                }
                }
                if (keyPair[1].isEmpty()) {
                    run loop@{
                        keysB.forEach {
                            Log.d("NFC","buildKeyMap()B$index _"+ bytes2Hex(it))
                            val auth = mMFC?.authenticateSectorWithKeyB(index, it)
                            if (auth == true) {
                                keyPair[1] = it
                                return@loop
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                if (isConnectedButTagLost()) {
                    close()
                }
                while (mMFC?.isConnected == false) {
                    delay(500)
                    connect()
                }
                continue
            }
        }
        Log.d("NFC","buildKeyMap() duration "+(System.currentTimeMillis()-startTime))

        return keysMap
    }
    private fun buildSectorKey(sectorIndex:Int,keysA:Array<ByteArray>,keysB:Array<ByteArray>):Array<ByteArray>?{

return null
    }

    fun isConnectedButTagLost(): Boolean {
        if (mMFC?.isConnected == true) {
            try {
                mMFC?.readBlock(0)
            } catch (e: IOException) {
                return true
            }
        }
        return false
    }

    /**
     * 写数据
     */
    suspend fun writeBlock(
        sectorIndex: Int,
        blockIndex: Int,
        data: ByteArray,
        keys: Array<ByteArray>
    ): Int {
        var code = 0
        if (mMFC == null || mMFC?.isConnected == false) {
            code = checkForTagAndCreate()
            if (code != 0) {
                return code
            }
        }
        if (mMFC == null) {
            return CREATE_MC_ERROR
        }
        if (sectorIndex >= mMFC!!.sectorCount) {
            return SECTOR_OUT
        }
        if (blockIndex >= mMFC!!.getBlockCountInSector(sectorIndex)) {
            return BLOCK_OUT
        }
        if (data.size != 16) {
            return BLOCK_DATA_ERROR
        }
        if (!authSectorKey(sectorIndex, keys[1], true)) {
            return KEY_AUTH_FAIL
        }
        mMFC?.let { mc ->
            var block = mc.sectorToBlock(sectorIndex) + blockIndex
            try {
                mc.writeBlock(block, data)
            } catch (e: IOException) {
                e.printStackTrace()
                code = WRITE_BLOCK_ERROR
            } catch (e: TagLostException) {
                e.printStackTrace()
                code = CONNECT_ERROR
            }
        }
        return code
    }

    /**
     * 验证key是否正确
     */
    fun authSectorKey(sectorIndex: Int, key: ByteArray, asKeyB: Boolean): Boolean {
        var res = false
        mMFC?.let { mc ->
            for (i in 0..1) {
                try {
                    if (asKeyB) {
                        res = mc.authenticateSectorWithKeyB(sectorIndex, key)
                    } else {
                        res = mc.authenticateSectorWithKeyA(sectorIndex, key)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: TagLostException) {
                    e.printStackTrace()
                }
                if (res) {
                    break
                }
            }
        }
        return res
    }

    fun bytes2Hex(bytes: ByteArray?): String? {
        val ret = StringBuilder()
        if (bytes != null) {
            for (b in bytes) {
                ret.append(String.format("%02X", b.toInt() and 0xFF))
            }
        }
        return ret.toString()
    }

    /**
     * Check if key B is readable.
     * Key B is readable for the following configurations:
     *
     *  * C1 = 0, C2 = 0, C3 = 0
     *  * C1 = 0, C2 = 0, C3 = 1
     *  * C1 = 0, C2 = 1, C3 = 0
     *
     *
     * @param ac The access conditions (4 bytes).
     * @return True if key B is readable. False otherwise.
     */
    private fun isKeyBReadable(ac: ByteArray?): Boolean {
        if (ac == null) {
            return false
        }
        val c1 = (ac[1].toInt() and 0x80 ushr 7).toByte()
        val c2 = (ac[2].toInt() and 0x08 ushr 3).toByte()
        val c3 = (ac[2].toInt() and 0x80 ushr 7).toByte()
        return (c1.toInt() == 0 && c2.toInt() == 0 && c3.toInt() == 0 || c2.toInt() == 1 && c3.toInt() == 0
                || c2.toInt() == 0 && c3.toInt() == 1)
    }

    /**
     * Convert a string of hex data into a byte array.
     * Original author is: Dave L. (http://stackoverflow.com/a/140861).
     *
     * @param hex The hex string to convert
     * @return An array of bytes with the values of the string.
     */
    fun hex2Bytes(hex: String?): ByteArray? {
        if (!(hex != null && hex.length % 2 == 0 && hex.matches(Regex("[0-9A-Fa-f]+")))) {
            return null
        }
        val len = hex.length
        val data = ByteArray(len / 2)
        try {
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
                i += 2
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.d(
                javaClass.name,
                "Argument(s) for hexStringToByteArray(String s)" + "was not a hex string"
            )
        }
        return data
    }
    fun isHex(hex: String?): Boolean {
        return hex != null && hex.length % 2 == 0 && hex.matches(Regex("[0-9A-Fa-f]+"))
    }
    fun codeMessage(code:Int):String{
        return when(code){
            0->"成功"
            ERROR->"未知错误"
            INTENT_TAG_NULL->"无TAG数据"
            TAG_DATA_ERROR->"无法解析TAG数据"
            DEVICE_NOT_SUPPORT->"设备不支持"
            TAG_NOT_SUPPORT->"不支持TAG协议"
            CREATE_MC_ERROR->"无法读取，请重新刷卡"
            CONNECT_ERROR->"无法读取，请重新刷卡"
            CONNECT_TIMEOUT->"无法读取，请重新刷卡"
            SECTOR_OUT->"扇区索引越界"
            BLOCK_OUT->"区块索引越界"
            BLOCK_DATA_ERROR->"区块数据异常"
            KEY_AUTH_FAIL->"密钥错误"
            else-> "未知错误"
        }
    }
}