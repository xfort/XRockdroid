package org.xfort.xrockdroid.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat


/**
 ** Created by ZhangHuaXin on 2021/11/10.
 **/
object XRockPermission {

    fun hasPermissions(ctx: Context, permissionArray: Array<String>): Array<String>? {
        val notGrantedPerms = arrayListOf<String>()

        permissionArray.forEach { perms ->
            if (ContextCompat.checkSelfPermission(
                    ctx, perms
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPerms.add(perms)
            }
        }
        return notGrantedPerms.toTypedArray()
    }

    /**
     * 检查权限
     */
    fun checkBackgroundLocationPermissions(ctx: Context): Array<String>? {
        val notGrantedPerms = arrayListOf<String>()
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
        ).forEach { perms ->
            if (ContextCompat.checkSelfPermission(
                    ctx, perms
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPerms.add(perms)
            }
        }
        return if (notGrantedPerms.isEmpty()) null else notGrantedPerms.toTypedArray()
    }

    fun hasBackgroundLocationPerm(ctx: Context): Boolean {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ).forEach { perms ->
            if (ContextCompat.checkSelfPermission(
                    ctx, perms
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    /**
     * 检查GPS 是否可用
     */
    fun gpsEnable(ctx: Context): Boolean {
        val obj = ctx.getSystemService(Context.LOCATION_SERVICE)
        if (obj != null && obj is LocationManager) {

            return obj.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        return false
    }

    /**
     * 位置设置
     */
    fun openLocationSetting(ctx: Context): Boolean {
        try {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            ctx.startActivity(settingsIntent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}