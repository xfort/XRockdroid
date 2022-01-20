package org.xfort.xrockdroid.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 ** Created by ZhangHuaXin on 2021/11/5.
 **/
object RockUtil {

    fun openBrowser(context: Context, url: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

    fun closeSoftInput(context: FragmentActivity) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }

    fun sendSMS(ctx: Context, mobile: String, body: CharSequence) {
        try {
            val smsManager = ContextCompat.getSystemService<SmsManager>(ctx, SmsManager::class.java)
            smsManager?.sendTextMessage(mobile, null, body.toString(), null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun String.toast(ctx: Context) {
    val msg = this
    GlobalScope.launch(Dispatchers.Main) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }
}

