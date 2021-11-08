package org.xfort.xrockmedia

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData

/**
 ** Created by ZhangHuaXin on 2021/7/16.
 **/
class MusicControllerCallback(
    val callback: MediaBrowserCompat.ConnectionCallback,
    val playstate: MutableLiveData<PlaybackStateCompat>,
    val position: MutableLiveData<Long>
) : MediaControllerCompat.Callback() {
    val TAG = javaClass.name

    var playState =
        PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 0f).build()
    var handler: Handler? = null

    var playingDuration = 0L
    override fun onSessionReady() {
        super.onSessionReady()
        handler = Handler(Looper.getMainLooper())
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        super.onPlaybackStateChanged(state)
        playState =
            state ?: PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
                .build()
        playstate.postValue(state)
        Log.d(TAG, "onPlaybackStateChanged()_" + state?.toString())
        updateProgress()
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        super.onMetadataChanged(metadata) //metadata?.bundle?.putLong()
        playingDuration = metadata?.getLong(METADATA_KEY_DURATION) ?: 0
        Log.d(TAG, "onMetadataChanged()_")
        val state = PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f)
            .setExtras(metadata?.bundle).build()
        playstate.postValue(state)
    }

    override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
        super.onAudioInfoChanged(info)
        Log.d(TAG, "onAudioInfoChanged()_" + info?.toString())
    }

    override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        super.onQueueChanged(queue)
        Log.d(TAG, "onQueueChanged()_" + queue?.toString())
    }

    override fun onSessionEvent(event: String?, extras: Bundle?) {
        super.onSessionEvent(event, extras)
        Log.d(TAG, "onSessionEvent()_" + event?.toString())
    }

    override fun onSessionDestroyed() {
        super.onSessionDestroyed()
        handler?.removeCallbacksAndMessages(null)
        Log.d(TAG, "onSessionDestroyed()")
        callback.onConnectionSuspended()
    }

    override fun binderDied() {
        super.binderDied()
        Log.d(TAG, "binderDied()")
        handler?.removeCallbacksAndMessages(null)
    }

    override fun onExtrasChanged(extras: Bundle?) {
        super.onExtrasChanged(extras)
        Log.d(TAG, "onExtrasChanged()")
    }

    fun updateProgress() {
        position.postValue(getCurrentPlayPosition())
        handler?.postDelayed({ updateProgress() }, 1000)
    }

    fun getCurrentPlayPosition(): Long {
        if (playState != null && (playState.state == PlaybackStateCompat.STATE_PLAYING || playState.state == PlaybackStateCompat.STATE_BUFFERING)) {
            val timeDelta = SystemClock.elapsedRealtime() - playState.lastPositionUpdateTime
            return (playState.position + (timeDelta * playState.playbackSpeed)).toLong()
        }
        return playState.position
    }
}