package org.xfort.xrockdroid.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log

/**
 ** Created by ZhangHuaXin on 2021/11/18.
 **/
abstract class XRockSMSReceiver : BroadcastReceiver() {
    val TAG = javaClass.simpleName

    @SuppressLint("NewApi")
    override fun onReceive(context: Context?, intent: Intent?) {
        val bd = intent?.extras?
        if (bd != null) {
            val smsFormat = bd.getString("format")
            val objArray = bd.get("pdus")
            if (objArray != null && objArray is Array<*>) {
                val isVersionM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                val msgArray = mutableListOf<SmsMessage>()
                for (itemObj in objArray) {
                    val smsMessage = if (isVersionM) {
                        SmsMessage.createFromPdu(itemObj as ByteArray, smsFormat)
                    } else {
                        SmsMessage.createFromPdu(itemObj as ByteArray)
                    }
                    Log.d(TAG, "msg_${smsMessage.originatingAddress}_${smsMessage.messageBody}")
                    msgArray.add(smsMessage)
                }
                onReceiveMessage(msgArray.toTypedArray())
            }
        }
    }

    abstract fun onReceiveMessage(smsMessage: Array<SmsMessage>)
}