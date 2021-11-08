package org.xfort.xrockmedia

import android.app.Service
import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

/**
 ** Created by ZhangHuaXin on 2021/7/19.
 **/
class PlayerEventListener(
    val musicNotify: XMusicNotification,
    val musicPlayer: Player,
    val service: Service
) : Player.Listener {
    val TAG = javaClass.name
    override fun onPlaybackStateChanged(state: Int) {
        Log.d(TAG, "onPlaybackStateChanged()_$state")
        when (state) {
            Player.STATE_BUFFERING, Player.STATE_READY -> {
                musicNotify.showNotificationForPlayer(musicPlayer)
                if (state == Player.STATE_READY) { // When playing/paused save the current media item in persistent
                    musicPlayer.duration
                }
            }
            else -> {
                //musicNotify.hideNotification()
            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        Log.d(TAG, "onPlayWhenReadyChanged()_$reason")

        if (!playWhenReady) { // If playback is paused we remove the foreground state which allows the
            // notification to be dismissed. An alternative would be to provide a
            // "close" button in the notification which stops playback and clears
            // the notification.
            service.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.d(TAG, "onPlayerError()")

        error.printStackTrace()
        var message = "error";
        when (error.errorCode) { // If the data from MediaSource object could not be loaded the Exoplayer raises
            // a type_source error.
            // An error message is printed to UI via Toast message to inform the user.
            ExoPlaybackException.TYPE_SOURCE -> {
                message = "R.string.error_media_not_found;"

                //Log.e(TAG, "TYPE_SOURCE: " + error.sourceException.message)
            } // If the error occurs in a render component, Exoplayer raises a type_remote error.
            ExoPlaybackException.TYPE_RENDERER -> { //Log.e(TAG, "TYPE_RENDERER: " + error.rendererException.message)
            } // If occurs an unexpected RuntimeException Exoplayer raises a type_unexpected error.
            ExoPlaybackException.TYPE_UNEXPECTED -> { //Log.e(TAG, "TYPE_UNEXPECTED: " + error.unexpectedException.message)
            } // Occurs when there is a OutOfMemory error.
            //                ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
            //                    Log.e(TAG, "TYPE_OUT_OF_MEMORY: " + error.outOfMemoryError.message)
            //                }
            // If the error occurs in a remote component, Exoplayer raises a type_remote error.
            ExoPlaybackException.TYPE_REMOTE -> { //Log.e(TAG, "TYPE_REMOTE: " + error.message)
            }
        }
        Toast.makeText(
            service.applicationContext, message, Toast.LENGTH_LONG
        ).show()
    }


}