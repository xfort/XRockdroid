package org.xfort.xrockdroid.net

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink

/**
 ** Created by ZhangHuaXin on 2021/6/23.
 * JSON 请求参数封装
 **/
class JsonBody(val jsonBytes: ByteArray?) : RequestBody() {
    private val JSON_CONTENT_TYPE = "application/json".toMediaType()
    val jsonData by lazy { JsonObject() }

    override fun contentType(): MediaType {
        return JSON_CONTENT_TYPE
    }

    fun set(key: String, value: String): JsonBody {
        jsonData.addProperty(key, value)
        return this
    }

    fun set(key: String, value: JsonElement): JsonBody {
        jsonData.add(key, value)
        return this
    }

    override fun contentLength(): Long {
        return writeOrCountBytes(null, true)
    }

    override fun writeTo(sink: BufferedSink) {
        writeOrCountBytes(sink, false)
    }

    private fun writeOrCountBytes(sink: BufferedSink?, countBytes: Boolean): Long {
        var byteCount = 0L
        val buffer: Buffer = if (countBytes) Buffer() else sink!!.buffer
        if (jsonBytes != null && jsonBytes.isNotEmpty()) {
            buffer.write(jsonBytes)
        } else if (jsonData.size() > 0) {
            buffer.writeUtf8(jsonData.toString())
        }
        if (countBytes) {
            byteCount = buffer.size
            buffer.clear()
        }
        return byteCount
    }
}