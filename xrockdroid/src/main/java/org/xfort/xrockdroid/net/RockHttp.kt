package org.xfort.xrockdroid.net

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.xfort.xrockdroid.BuildConfig
import java.io.StringReader

/**
 ** Created by ZhangHuaXin on 2021/6/23.
 * okhttp 的封装
 **/
class RockHttp private constructor() {
    lateinit var okHttpClient: OkHttpClient
    lateinit var gson: Gson

    init {
        gson = GsonBuilder().disableHtmlEscaping().create()
        okHttpClient = createOKHttp()
    }

    companion object {
        val Instance: RockHttp by lazy {
            RockHttp()
        }
    }

    fun createOKHttp(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val httpLog = HttpLoggingInterceptor()
            httpLog.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(httpLog)
        }
        return builder.build()
    }

    inline fun <reified T> doRequest(request: Request): T? {
        val target = T::class.java
        var result: T? = null

        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val data = response.body?.string()?.trim()
                if (!data.isNullOrEmpty()) {
                    result = gson.fromJson(StringReader(data), target)
                } else {
                    Log.e(javaClass.name, "http_response null ${response.code}")
                }
            } else {
                Log.e(javaClass.name, "http_State error ${response.code}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
        return result
    }

    inline fun <reified T> get(url: String, headers: Headers): T? {
        val request = Request.Builder().get().url(url).headers(headers).build()
        return doRequest(request)
    }

    inline fun <reified T> post(url: String, headers: Headers, body: RequestBody): T? {
        val request = Request.Builder().post(body).url(url).headers(headers).build()
        return doRequest(request)
    }

}