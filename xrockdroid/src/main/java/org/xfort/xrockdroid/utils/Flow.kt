package org.xfort.xrockdroid.utils

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

/**
 ** Created by ZhangHuaXin on 2021/11/8.
 **/
class Flow {}

fun <T> SingleSharedFlow(): MutableSharedFlow<T> {
    return MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
}

inline fun <T> MutableSharedFlow<T>.collectWithScope(
    scope: LifecycleCoroutineScope,
    crossinline action: suspend (value: T) -> Unit
): Job {
    return scope.launchWhenCreated {
        collect(action)
    }
}