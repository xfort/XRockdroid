package org.xfort.xrockmedia

import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
//import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

/**
 ** Created by ZhangHuaXin on 2021/7/16.
 **/
class MusicQueueNavigator(
    mediaSession: MediaSessionCompat, val playbackpre: MusicPlayBackPreparer
) : TimelineQueueNavigator(mediaSession) {
    val TAG = javaClass.name

    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        Log.d(TAG, "getMediaDescription()")

        playbackpre.currentMediaMetadataCompat?.let {
            return it.description
        }

        return MediaDescriptionCompat.Builder().setTitle("NULL").setSubtitle("NULL").build()
    }

    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean {
        return super.onCommand(player, command, extras, cb)
    }

    //override fun onCommand(
    //    player: Player,
    //    controlDispatcher: ControlDispatcher,
    //    command: String,
    //    extras: Bundle?,
    //    cb: ResultReceiver?
    //): Boolean {
    //    return super.onCommand(player, controlDispatcher, command, extras, cb)
    //}

    override fun getSupportedQueueNavigatorActions(player: Player): Long {
        return super.getSupportedQueueNavigatorActions(player)
    }

    override fun onSkipToPrevious(player: Player) {
        super.onSkipToPrevious(player)
    }

    override fun onSkipToQueueItem(player: Player, id: Long) {
        super.onSkipToQueueItem(player,  id)
    }

    override fun onSkipToNext(player: Player) {
        super.onSkipToNext(player)
    }
}