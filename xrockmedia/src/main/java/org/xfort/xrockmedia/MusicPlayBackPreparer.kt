package org.xfort.xrockmedia

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

/**
 ** Created by ZhangHuaXin on 2021/7/16.
 **/
class MusicPlayBackPreparer(val player: ExoPlayer) : MediaSessionConnector.PlaybackPreparer {
    val TAG = javaClass.name

    var currentMediaMetadataCompat: MediaMetadataCompat? = null

    override fun onCommand(
        player: Player, command: String, extras: Bundle?, cb: ResultReceiver?
    ): Boolean {
        return false
    } //override fun onCommand(
    //    player: Player,
    //    controlDispatcher: ControlDispatcher,
    //    command: String,
    //    extras: Bundle?,
    //    cb: ResultReceiver?
    //): Boolean {
    //    Log.d(TAG, "onCommand()")
    //    return false
    //}

    override fun getSupportedPrepareActions(): Long {
        Log.d(TAG, "getSupportedPrepareActions()")
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or PlaybackStateCompat.ACTION_PLAY_FROM_URI or PlaybackStateCompat.ACTION_PREPARE_FROM_URI
    }

    override fun onPrepare(playWhenReady: Boolean) {
        Log.d(TAG, "onPrepare()")

    }

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromMediaId()")

    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromSearch()")
        MediaMetadataCompat.CREATOR

    }

    override fun onPrepareFromUri(
        uri: Uri, playWhenReady: Boolean, extras: Bundle?
    ) {
        Log.d(TAG, "onPrepareFromUri() ")
        currentMediaMetadataCompat = null

        val argMediaId=extras?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?:uri.toString()
        val metaData = MediaMetadataCompat.Builder().apply {
            mediaID = argMediaId
            mediaUri = uri.toString()
        }

        if (extras != null) {
            metaData.extras =
                extras //title = extras.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
            //title += extras.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
            //extras.keySet().forEach { key ->
            //    metaData.putString(key, extras.getString(key))
            //}
            currentMediaMetadataCompat = metaData.build()
        }

        val mediaItem = MediaItem.Builder().setUri(uri).setMediaId(argMediaId).build()

        player.stop()
        player.clearMediaItems()

        player.playWhenReady = true

        player.setMediaItem(mediaItem, 0)
        player.prepare()
    }

}