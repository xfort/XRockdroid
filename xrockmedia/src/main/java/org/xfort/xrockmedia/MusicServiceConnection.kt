package org.xfort.xrockmedia

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.xfort.xrockmedia.MusicControllerCallback

/**
 ** Created by ZhangHuaXin on 2021/7/16.
 **/
class MusicServiceConnection(val context: Context, serviceComponent: ComponentName) {
    val TAG = javaClass.name

    lateinit var mediaController: MediaControllerCompat

    val mediaBrowConnectCallback = MusicConnectCallback(context)

    private val mediaBrow =
        MediaBrowserCompat(context, serviceComponent, mediaBrowConnectCallback, null).apply {
            connect()
        }
    val playStateCallback by lazy { MutableLiveData<PlaybackStateCompat>() }
    val positionCallback by lazy { MutableLiveData<Long>() }

    companion object {
        @Volatile
        private var instance: MusicServiceConnection? = null
        fun getInstance(context: Context, serviceComponent: ComponentName) =
            instance ?: synchronized(this) {
                instance ?: MusicServiceConnection(
                    context, serviceComponent
                )
            }
    }

    inner class MusicConnectCallback(context: Context) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d(TAG, "onConnected()")
            mediaController = MediaControllerCompat(context, mediaBrow.sessionToken).apply {
                registerCallback(
                    MusicControllerCallback(
                        mediaBrowConnectCallback, playStateCallback, positionCallback
                    )
                )
            }
        }


        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            Log.d(TAG, "onConnectionSuspended()")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Log.d(TAG, "onConnectionFailed()")
        }
    }


    fun sendCommand(
        command: String, parameters: Bundle?, resultCallback: ((Int, Bundle?) -> Unit)
    ) = if (mediaBrow.isConnected) {
        mediaController.sendCommand(command, parameters, object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                resultCallback(resultCode, resultData)
            }
        })
        true
    } else {
        false
    }

    fun playMediaFromUri(uri: Uri, title: String, subTitle: String, mimeType: String) {
        if (mediaBrow.isConnected) {
            val extras = Bundle()
            extras.putCharSequence(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            extras.putCharSequence(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subTitle)
            extras.putCharSequence(METADATA_KEY_MIME_TYPE, mimeType)
            playMediaFromUri(uri, extras)
        }
    }

    fun playMediaFromUri(uri: Uri, extras: Bundle) {
        if (mediaBrow.isConnected) {
            mediaController.transportControls.playFromUri(uri, extras)
        }
    }

    fun startOrPause() {
        if (!mediaBrow.isConnected) {
            return
        }
        val currentState = mediaController.playbackState.state
        if (currentState == PlaybackStateCompat.STATE_PLAYING || currentState == PlaybackStateCompat.STATE_BUFFERING) {
            mediaController.transportControls.pause()
        } else {
            val actions = mediaController.playbackState.actions
            if (actions and PlaybackStateCompat.ACTION_PLAY != 0L || (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L || currentState == PlaybackStateCompat.STATE_PAUSED)) {
                mediaController.transportControls.play()
            }
        }
    }

    fun seek(position: Long) {
        if (!mediaBrow.isConnected) {
            return
        }
        val currentState = mediaController.playbackState.state
        if (currentState == PlaybackStateCompat.STATE_BUFFERING
            || currentState == PlaybackStateCompat.STATE_PLAYING
            || currentState == PlaybackStateCompat.STATE_PAUSED ) {
            mediaController.transportControls.seekTo(position)
        }
    }
}
