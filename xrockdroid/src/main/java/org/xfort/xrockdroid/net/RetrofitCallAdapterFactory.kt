package org.xfort.xrock.net

import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 ** Created by ZhangHuaXin on 2021/4/2.
 * retrofit call转换，现主用于 解决 http.status!=200时引发协程异常闪退问题
 **/
abstract class RetrofitCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java || returnType !is ParameterizedType || returnType.actualTypeArguments.size != 1) {
            return null
        }
        val delegate = retrofit.nextCallAdapter(this, returnType, annotations)
        @Suppress("UNCHECKED_CAST")
        return ErrorsCallAdapter(
            delegateAdapter = delegate as CallAdapter<Any, Call<*>>
        )
    }

    class ErrorsCallAdapter(
        private val delegateAdapter: CallAdapter<Any, Call<*>>
    ) : CallAdapter<Any, Call<*>> by delegateAdapter {

        override fun adapt(call: Call<Any>): Call<*> {
            return delegateAdapter.adapt(CallWithErrorHandling(call))
        }
    }

    class CallWithErrorHandling(
        private val delegate: Call<Any>
    ) : Call<Any> by delegate {

        override fun enqueue(callback: Callback<Any>) {
            delegate.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        callback.onResponse(call, response)
                    } else {
//            TODO        val resp = Resp<Any>(status = response.code(), message = "HttpError" + response.code(), data = null)
//                        callback.onResponse(call, Response.success(resp))
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    if (!call.isCanceled) {
//                TODO        val resp = Resp<Any>(status = 700, message = "Failure", data = null)
//                        callback.onResponse(call, Response.success(resp))
                    }
                    t.printStackTrace()
                }
            })
        }

        override fun clone() = CallWithErrorHandling(delegate.clone())
    }
}