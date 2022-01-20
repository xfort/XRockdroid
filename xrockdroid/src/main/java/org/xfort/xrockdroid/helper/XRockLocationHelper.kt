package org.xfort.xrockdroid.helper

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import org.xfort.xrockdroid.utils.XRockPermission
import java.util.concurrent.Executors

/**
 ** Created by ZhangHuaXin on 2021/11/10.
 **/
class XRockLocationHelper(val ctx: Context) {
    val TAG = javaClass.simpleName

    private val locationManager by lazy { ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    private val locationCallback: LocationListenerCompat


    init {
        locationCallback = object : LocationListenerCompat {
            override fun onLocationChanged(location: Location) {
                Log.d(TAG, "onLocationChanged() ${location.latitude} ${location.longitude}")
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
                super.onStatusChanged(provider, status, extras)
                Log.d(TAG, "onStatusChanged() ${provider} ${status}")
            }

            override fun onProviderEnabled(provider: String) {
                super.onProviderEnabled(provider)
                Log.d(TAG, "onProviderEnabled() ${provider} ")
            }

            override fun onProviderDisabled(provider: String) {
                super.onProviderDisabled(provider)
                Log.d(TAG, "onProviderDisabled() ${provider} ")
            }
        }
    }

    fun startLocation(): Boolean {
        if (!XRockPermission.hasBackgroundLocationPerm(ctx) || !XRockPermission.gpsEnable(ctx)) {
            Log.e(TAG, "没有位置权限 或 GPS 不可用")
            return false
        }

        if (ContextCompat.checkSelfPermission(
                ctx, permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                ctx, permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val req = LocationRequestCompat.Builder(10000).build()
        val exec = Executors.newSingleThreadExecutor()
        LocationManagerCompat.requestLocationUpdates(
            locationManager, LocationManager.GPS_PROVIDER, req, exec, locationCallback
        )
        return true
    }

    fun stop() {
        if (ContextCompat.checkSelfPermission(
                ctx, permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                ctx, permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationManagerCompat.removeUpdates(locationManager, locationCallback)
    }

}